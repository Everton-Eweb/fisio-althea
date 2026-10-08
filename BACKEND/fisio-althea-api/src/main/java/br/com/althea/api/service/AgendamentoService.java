package br.com.althea.api.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.althea.api.dto.AgendamentoRequestDTO;
import br.com.althea.api.dto.AgendamentoResponseDTO;
import br.com.althea.api.dto.ReagendamentoRequestDTO;

import br.com.althea.api.entity.Agendamento;
import br.com.althea.api.entity.DiaSemana;
import br.com.althea.api.entity.Disponibilidade;
import br.com.althea.api.entity.Paciente;
import br.com.althea.api.entity.Profissional;
import br.com.althea.api.entity.Servico;
import br.com.althea.api.entity.StatusAgendamento;
import br.com.althea.api.entity.TipoPerfil;

import br.com.althea.api.repository.AgendamentoRepository;
import br.com.althea.api.repository.DisponibilidadeRepository;
import br.com.althea.api.repository.PacienteRepository;
import br.com.althea.api.repository.ProfissionalRepository;
import br.com.althea.api.repository.ServicoRepository;

import jakarta.persistence.EntityManager;

import br.com.althea.api.exception.RecursoNaoEncontradoException;

/*
 * Camada de serviço responsável pelas operações
 * relacionadas aos agendamentos da Clínica Althea.
 *
 * Esta classe concentra regras de negócio importantes,
 * evitando que essas regras fiquem dentro do Controller.
 *
 * Fluxo:
 *
 * Controller
 *      ↓
 * AgendamentoService
 *      ↓
 * Repositories
 *      ↓
 * MySQL
 *
 * Tópicos:
 * - Engenharia de Software
 * - Análise e Projeto de Sistemas
 * - Arquitetura de Software
 * - Programação Orientada a Objetos
 * - Spring Framework
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Service
public class AgendamentoService {

        /*
         * Repository principal do agendamento.
         */
        private final AgendamentoRepository agendamentoRepository;

        /*
         * Repositories utilizados para localizar
         * as entidades relacionadas ao agendamento.
         */
        private final PacienteRepository pacienteRepository;
        private final ProfissionalRepository profissionalRepository;
        private final ServicoRepository servicoRepository;

        /*
         * Utilizado para verificar se o profissional
         * possui disponibilidade no dia e horário solicitados.
         */
        private final DisponibilidadeRepository disponibilidadeRepository;

        /*
         * Permite atualizar a entidade Java com valores
         * que foram gerados diretamente pelo banco,
         * como o campo criado_em.
         */
        private final EntityManager entityManager;

        /*
         * Verifica se dois intervalos de atendimento
         * possuem sobreposição.
         *
         * Exemplo:
         *
         * Existente: 09:00 - 10:00
         * Novo: 09:30 - 10:30
         *
         * Resultado:
         * conflito = true
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         */
        private boolean possuiSobreposicao(
                        LocalTime novoInicio,
                        int novaDuracaoMinutos,
                        Agendamento agendamentoExistente) {

                LocalTime novoFim = novoInicio.plusMinutes(
                                novaDuracaoMinutos);

                LocalTime existenteInicio = agendamentoExistente.getHora();

                int duracaoExistente = agendamentoExistente
                                .getServico()
                                .getDuracaoMinutos();

                LocalTime existenteFim = existenteInicio.plusMinutes(
                                duracaoExistente);

                return novoInicio.isBefore(existenteFim)
                                &&
                                novoFim.isAfter(existenteInicio);
        }

        /*
         * Verifica se o atendimento inteiro cabe
         * dentro de alguma faixa de disponibilidade.
         *
         * Não basta verificar somente se o horário
         * inicial está dentro da faixa.
         *
         * Exemplo:
         *
         * disponibilidade:
         * 08:00 - 12:00
         *
         * novo atendimento:
         * 11:30 - 12:30
         *
         * Apesar de 11:30 estar dentro da faixa,
         * o atendimento termina após 12:00.
         *
         * Portanto, não é permitido.
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        private boolean atendimentoCabeNaDisponibilidade(
                        LocalTime horaInicio,
                        int duracaoMinutos,
                        List<Disponibilidade> disponibilidades) {

                /*
                 * Calcula o horário de término
                 * do atendimento solicitado.
                 */
                LocalTime horaFim = horaInicio.plusMinutes(
                                duracaoMinutos);

                /*
                 * O atendimento é válido quando existe
                 * pelo menos uma faixa em que:
                 *
                 * horaInicio >= início da faixa
                 *
                 * e
                 *
                 * horaFim <= fim da faixa
                 */
                return disponibilidades
                                .stream()
                                .anyMatch(disponibilidade -> {

                                        LocalTime inicioFaixa = disponibilidade.getHoraInicio();

                                        LocalTime fimFaixa = disponibilidade.getHoraFim();

                                        boolean inicioValido = !horaInicio.isBefore(
                                                        inicioFaixa);

                                        boolean fimValido = !horaFim.isAfter(
                                                        fimFaixa);

                                        return inicioValido
                                                        && fimValido;
                                });
        }

        /*
         * Injeção de dependência pelo construtor.
         *
         * O Spring fornece automaticamente
         * todos os repositories necessários.
         */
        public AgendamentoService(
                        AgendamentoRepository agendamentoRepository,
                        PacienteRepository pacienteRepository,
                        ProfissionalRepository profissionalRepository,
                        ServicoRepository servicoRepository,
                        DisponibilidadeRepository disponibilidadeRepository,
                        EntityManager entityManager) {

                this.agendamentoRepository = agendamentoRepository;
                this.pacienteRepository = pacienteRepository;
                this.profissionalRepository = profissionalRepository;
                this.servicoRepository = servicoRepository;
                this.disponibilidadeRepository = disponibilidadeRepository;
                this.entityManager = entityManager;
        }

        /*
         * Retorna todos os agendamentos cadastrados.
         *
         * Antes de devolver os dados pela API,
         * cada entidade é convertida para DTO.
         */
        public List<AgendamentoResponseDTO> listarTodos() {

                return agendamentoRepository.findAll()
                                .stream()
                                .map(this::converterParaDTO)
                                .toList();
        }

        /*
         * Busca um agendamento específico pelo ID.
         */
        public Optional<AgendamentoResponseDTO> buscarPorId(Long id) {

                return agendamentoRepository.findById(id)
                                .map(this::converterParaDTO);
        }

        /*
         * Cria um novo agendamento.
         *
         * @Transactional garante que a operação seja
         * executada como uma única transação.
         *
         * Caso ocorra algum erro durante o processo,
         * nenhuma alteração parcial deverá permanecer.
         */
        @Transactional
        public AgendamentoResponseDTO cadastrar(
                        AgendamentoRequestDTO dados) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O PACIENTE
                 * -------------------------------------------------
                 *
                 * Não podemos criar um agendamento para
                 * um paciente inexistente.
                 */
                Paciente paciente = pacienteRepository
                                .findById(dados.getIdPaciente())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Paciente não encontrado."));

                /*
                 * -------------------------------------------------
                 * 2. LOCALIZA O PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = profissionalRepository
                                .findById(dados.getIdProfissional())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Profissional não encontrado."));

                /*
                 * -------------------------------------------------
                 * 3. LOCALIZA O SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = servicoRepository
                                .findById(dados.getIdServico())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Serviço não encontrado."));

                /*
                 * -------------------------------------------------
                 * 4. DESCOBRE O DIA DA SEMANA
                 * -------------------------------------------------
                 *
                 * A data recebida pode ser, por exemplo:
                 *
                 * 2026-09-14
                 *
                 * O Java identifica que essa data corresponde
                 * a uma SEGUNDA-FEIRA.
                 */
                DiaSemana diaSemana = converterDiaSemana(dados.getData().getDayOfWeek());

                /*
                 * -------------------------------------------------
                 * 5. VERIFICA A DISPONIBILIDADE DO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * Busca todas as disponibilidades ativas
                 * daquele profissional naquele dia da semana.
                 */
                List<Disponibilidade> disponibilidades = disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                profissional.getIdProfissional(),
                                                diaSemana);

                /*
                 * -------------------------------------------------
                 * VERIFICA A DISPONIBILIDADE COMPLETA
                 * -------------------------------------------------
                 *
                 * Não basta o atendimento COMEÇAR dentro
                 * da faixa disponível.
                 *
                 * Ele precisa terminar dentro dela também.
                 *
                 * Exemplo:
                 *
                 * disponibilidade:
                 * 08:00 - 12:00
                 *
                 * serviço:
                 * 60 minutos
                 *
                 * 11:00 → 12:00 = permitido
                 * 11:30 → 12:30 = bloqueado
                 *
                 * Tópicos:
                 * - Levantamento e Especificação de Requisitos
                 * - Engenharia de Software
                 * - Arquitetura de Software
                 * - Programação Orientada a Objetos
                 */
                boolean horarioDisponivel = atendimentoCabeNaDisponibilidade(
                                dados.getHora(),
                                servico.getDuracaoMinutos(),
                                disponibilidades);

                /*
                 * Caso o intervalo completo não caiba
                 * dentro de nenhuma faixa cadastrada,
                 * o agendamento não pode ser criado.
                 */
                if (!horarioDisponivel) {

                        throw new IllegalArgumentException(
                                        "O atendimento não cabe dentro do período de disponibilidade do profissional.");
                }

                /*
                 * /*
                 * -------------------------------------------------
                 * VERIFICA CONFLITO DE HORÁRIOS
                 * -------------------------------------------------
                 *
                 * Não verificamos apenas se existe outro
                 * agendamento com a mesma hora inicial.
                 *
                 * Agora comparamos os intervalos completos,
                 * considerando a duração dos serviços.
                 *
                 * Exemplo:
                 *
                 * Existente:
                 * 09:00 - 10:00
                 *
                 * Novo:
                 * 09:30 - 10:30
                 *
                 * Mesmo com horários iniciais diferentes,
                 * existe sobreposição.
                 *
                 * Tópicos:
                 * - Levantamento e Especificação de Requisitos
                 * - Engenharia de Software
                 * - Spring Data JPA
                 * - Banco de Dados Relacional
                 * - Programação Orientada a Objetos
                 */
                List<Agendamento> agendamentosExistentes = agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                dados.getIdProfissional(),
                                                dados.getData(),
                                                StatusAgendamento.CANCELADO);

                /*
                 * Recupera a duração do serviço
                 * que está sendo agendado.
                 */
                int duracaoNovoServico = servico.getDuracaoMinutos();

                /*
                 * Verifica se o intervalo do novo atendimento
                 * entra em conflito com qualquer atendimento
                 * já existente.
                 */
                boolean existeConflito = agendamentosExistentes
                                .stream()
                                .anyMatch(agendamentoExistente ->

                                possuiSobreposicao(
                                                dados.getHora(),
                                                duracaoNovoServico,
                                                agendamentoExistente));

                /*
                 * Caso exista qualquer sobreposição,
                 * o agendamento não pode ser criado.
                 */
                if (existeConflito) {

                        throw new IllegalArgumentException(
                                        "O horário informado entra em conflito com outro agendamento do profissional.");
                }

                /*
                 * -------------------------------------------------
                 * 7. CRIA O AGENDAMENTO
                 * -------------------------------------------------
                 */
                Agendamento agendamento = new Agendamento();

                /*
                 * Define os relacionamentos.
                 *
                 * No banco isso preencherá:
                 *
                 * id_paciente
                 * id_profissional
                 * id_servico
                 */
                agendamento.setPaciente(paciente);
                agendamento.setProfissional(profissional);
                agendamento.setServico(servico);

                /*
                 * Define data, horário e observação.
                 */
                agendamento.setData(dados.getData());
                agendamento.setHora(dados.getHora());
                agendamento.setObservacao(dados.getObservacao());

                /*
                 * Todo novo agendamento começa
                 * automaticamente como AGENDADO.
                 *
                 * O cliente da API não escolhe esse status.
                 */
                agendamento.setStatus(StatusAgendamento.AGENDADO);

                /*
                 * -------------------------------------------------
                 * 8. SALVA NO BANCO
                 * -------------------------------------------------
                 */
                /*
                 * saveAndFlush força o INSERT no banco
                 * antes de montarmos a resposta.
                 */
                Agendamento agendamentoSalvo = agendamentoRepository.saveAndFlush(agendamento);

                /*
                 * O MySQL preenche criado_em através
                 * do CURRENT_TIMESTAMP.
                 *
                 * refresh atualiza o objeto Java com
                 * o valor efetivamente gerado pelo banco.
                 */
                entityManager.refresh(agendamentoSalvo);

                /*
                 * Converte a entidade salva em DTO
                 * para devolver uma resposta organizada.
                 */
                return converterParaDTO(agendamentoSalvo);
        }

        /*
         * Confirma um agendamento existente.
         *
         * O agendamento é localizado pelo ID e
         * seu status é alterado para CONFIRMADO.
         *
         * Tópicos:
         * - Engenharia de Software
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Levantamento e Especificação de Requisitos
         */
        @Transactional
        public AgendamentoResponseDTO confirmar(Long id) {

                /*
                 * Procura o agendamento pelo identificador.
                 *
                 * Caso não exista, interrompe a operação.
                 */
                Agendamento agendamento = agendamentoRepository
                                .findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Agendamento não encontrado."));

                /*
                 * Altera o status do agendamento.
                 *
                 * Antes:
                 * AGENDADO
                 *
                 * Depois:
                 * CONFIRMADO
                 */
                agendamento.setStatus(StatusAgendamento.CONFIRMADO);

                /*
                 * Persiste a alteração no banco de dados.
                 */
                Agendamento agendamentoAtualizado = agendamentoRepository.save(agendamento);

                /*
                 * Retorna o agendamento atualizado
                 * utilizando o DTO de resposta.
                 */
                return converterParaDTO(agendamentoAtualizado);
        }

        /*
         * Confirma um agendamento considerando
         * o usuário e o perfil autenticados.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode confirmar qualquer agendamento.
         *
         * RECEPCIONISTA
         * -> pode confirmar qualquer agendamento.
         *
         * PROFISSIONAL
         * -> pode confirmar somente um agendamento
         * atribuído a ele próprio.
         *
         * PACIENTE
         * -> não possui permissão para confirmar.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public AgendamentoResponseDTO confirmarComAutorizacao(
                        Long id,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O AGENDAMENTO
                 * -------------------------------------------------
                 */
                Agendamento agendamento = agendamentoRepository
                                .findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Agendamento não encontrado."));

                /*
                 * -------------------------------------------------
                 * 2. BLOQUEIA PACIENTE
                 * -------------------------------------------------
                 *
                 * A confirmação será realizada pelos perfis
                 * administrativos ou pelo profissional.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        throw new IllegalArgumentException(
                                        "O paciente não possui permissão para confirmar agendamentos.");
                }

                /*
                 * -------------------------------------------------
                 * 3. VALIDA O PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * Se o usuário autenticado for PROFISSIONAL,
                 * verificamos se o agendamento realmente
                 * pertence a ele.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        Long idUsuarioDoProfissional = agendamento
                                        .getProfissional()
                                        .getUsuario()
                                        .getIdUsuario();

                        /*
                         * Compara:
                         *
                         * usuário do JWT
                         * X
                         * usuário do profissional do agendamento
                         */
                        if (!idUsuarioAutenticado.equals(idUsuarioDoProfissional)) {

                                throw new IllegalArgumentException(
                                                "O profissional só pode confirmar os próprios agendamentos.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 4. CONFIRMA
                 * -------------------------------------------------
                 *
                 * Reutilizamos o método já existente.
                 */
                return confirmar(id);
        }

        /*
         * Cancela um agendamento considerando
         * o usuário e o perfil autenticados.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode cancelar qualquer agendamento.
         *
         * RECEPCIONISTA
         * -> pode cancelar qualquer agendamento.
         *
         * PACIENTE
         * -> pode cancelar somente os próprios agendamentos.
         *
         * PROFISSIONAL
         * -> pode cancelar somente os agendamentos
         * atribuídos a ele.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public AgendamentoResponseDTO cancelarComAutorizacao(
                        Long id,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O AGENDAMENTO
                 * -------------------------------------------------
                 */
                Agendamento agendamento = agendamentoRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Agendamento não encontrado."));

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 *
                 * O paciente somente pode cancelar
                 * um agendamento pertencente a ele próprio.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        Long idUsuarioDoPaciente = agendamento
                                        .getPaciente()
                                        .getUsuario()
                                        .getIdUsuario();

                        if (!idUsuarioAutenticado.equals(idUsuarioDoPaciente)) {

                                throw new IllegalArgumentException(
                                                "O paciente só pode cancelar os próprios agendamentos.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * O profissional somente pode cancelar
                 * atendimentos atribuídos a ele.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        Long idUsuarioDoProfissional = agendamento
                                        .getProfissional()
                                        .getUsuario()
                                        .getIdUsuario();

                        if (!idUsuarioAutenticado.equals(idUsuarioDoProfissional)) {

                                throw new IllegalArgumentException(
                                                "O profissional só pode cancelar os próprios agendamentos.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 4. CANCELA
                 * -------------------------------------------------
                 *
                 * ADMINISTRADOR e RECEPCIONISTA chegam
                 * diretamente até aqui.
                 *
                 * PACIENTE e PROFISSIONAL chegam apenas
                 * se forem proprietários do agendamento.
                 *
                 * Reutilizamos a regra já implementada.
                 */
                return cancelar(id);
        }

        /*
         * Cancela um agendamento existente.
         *
         * Ao cancelar, o status é alterado para CANCELADO.
         *
         * Como a verificação de conflito ignora registros
         * com esse status, o horário volta a ficar disponível
         * para novos agendamentos.
         *
         * RN03:
         * Ao cancelar um agendamento, o sistema deve
         * liberar novamente o horário correspondente.
         *
         * Tópicos:
         * - Engenharia de Software
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Levantamento e Especificação de Requisitos
         */
        @Transactional
        public AgendamentoResponseDTO cancelar(Long id) {

                /*
                 * Busca o agendamento pelo identificador.
                 *
                 * Caso ele não exista, a operação
                 * é interrompida.
                 */
                Agendamento agendamento = agendamentoRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Agendamento não encontrado."));

                // Impede cancelar novamente um atendimento já cancelado.
                if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
                        throw new IllegalArgumentException(
                                        "Este agendamento já está cancelado.");
                }

                /*
                 * Altera o status para CANCELADO.
                 */
                agendamento.setStatus(StatusAgendamento.CANCELADO);

                /*
                 * Salva a alteração no banco.
                 */
                Agendamento agendamentoCancelado = agendamentoRepository.save(agendamento);

                /*
                 * Retorna o registro atualizado
                 * utilizando o DTO de resposta.
                 */
                return converterParaDTO(agendamentoCancelado);
        }

        /*
         * Reagenda um atendimento considerando
         * o usuário e o perfil autenticados.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode reagendar qualquer agendamento.
         *
         * RECEPCIONISTA
         * -> pode reagendar qualquer agendamento.
         *
         * PACIENTE
         * -> pode reagendar somente os próprios.
         *
         * PROFISSIONAL
         * -> pode reagendar somente os atendimentos
         * atribuídos a ele.
         *
         * Depois da autorização, continuam sendo
         * verificadas as regras da RN04:
         *
         * - disponibilidade do profissional;
         * - conflito com outro agendamento.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         * - Levantamento e Especificação de Requisitos
         */
        @Transactional
        public AgendamentoResponseDTO reagendarComAutorizacao(
                        Long id,
                        ReagendamentoRequestDTO dados,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O AGENDAMENTO
                 * -------------------------------------------------
                 */
                Agendamento agendamento = agendamentoRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Agendamento não encontrado."));

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 *
                 * O paciente só pode reagendar
                 * um atendimento pertencente a ele.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        Long idUsuarioDoPaciente = agendamento
                                        .getPaciente()
                                        .getUsuario()
                                        .getIdUsuario();

                        if (!idUsuarioAutenticado.equals(idUsuarioDoPaciente)) {

                                throw new IllegalArgumentException(
                                                "O paciente só pode reagendar os próprios agendamentos.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * O profissional só pode reagendar
                 * um atendimento atribuído a ele.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        Long idUsuarioDoProfissional = agendamento
                                        .getProfissional()
                                        .getUsuario()
                                        .getIdUsuario();

                        if (!idUsuarioAutenticado.equals(idUsuarioDoProfissional)) {

                                throw new IllegalArgumentException(
                                                "O profissional só pode reagendar os próprios agendamentos.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 4. REAGENDA
                 * -------------------------------------------------
                 *
                 * Depois da autorização, reutilizamos
                 * o método já implementado.
                 *
                 * Ele continuará verificando:
                 *
                 * - disponibilidade;
                 * - conflito;
                 * - nova data;
                 * - novo horário.
                 */
                return reagendar(id, dados);
        }

        /*
         * Reagenda um atendimento existente.
         *
         * Antes de alterar a data e o horário,
         * o sistema verifica:
         *
         * 1. se o agendamento existe;
         * 2. se o profissional possui disponibilidade
         * na nova data e horário;
         * 3. se não existe OUTRO agendamento
         * ocupando o novo horário.
         *
         * RN04:
         * O reagendamento somente pode ocorrer quando
         * o novo horário estiver disponível.
         *
         * Tópicos:
         * - Engenharia de Software
         * - Análise e Projeto de Sistemas
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Levantamento e Especificação de Requisitos
         */
        @Transactional
        public AgendamentoResponseDTO reagendar(
                        Long id,
                        ReagendamentoRequestDTO dados) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * O sistema precisa saber qual atendimento
                 * será alterado.
                 */
                Agendamento agendamento = agendamentoRepository
                                .findById(id)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Agendamento não encontrado."));

                // Impede reagendar um atendimento que já foi cancelado.
                if (agendamento.getStatus() == StatusAgendamento.CANCELADO) {
                        throw new IllegalArgumentException(
                                        "Não é possível reagendar um atendimento cancelado.");
                }

                /*
                 * -------------------------------------------------
                 * 2. IDENTIFICA O NOVO DIA DA SEMANA
                 * -------------------------------------------------
                 *
                 * A disponibilidade está cadastrada utilizando
                 * SEGUNDA, TERCA, QUARTA etc.
                 *
                 * Por isso precisamos converter a nova data
                 * para o enum DiaSemana do projeto.
                 */
                DiaSemana novoDiaSemana = converterDiaSemana(
                                dados.getData().getDayOfWeek());

                /*
                 * -------------------------------------------------
                 * 3. BUSCA A DISPONIBILIDADE DO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * O profissional continua sendo o mesmo
                 * do agendamento original.
                 */
                List<Disponibilidade> disponibilidades = disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                agendamento
                                                                .getProfissional()
                                                                .getIdProfissional(),

                                                novoDiaSemana);

                /*
                 * -------------------------------------------------
                 * VERIFICA A DISPONIBILIDADE NO REAGENDAMENTO
                 * -------------------------------------------------
                 *
                 * O novo atendimento precisa caber completamente
                 * dentro da faixa de disponibilidade.
                 *
                 * Não basta a nova hora de início estar dentro
                 * da faixa.
                 *
                 * Exemplo:
                 *
                 * disponibilidade:
                 * 08:00 - 12:00
                 *
                 * serviço:
                 * 60 minutos
                 *
                 * 11:00 → 12:00 = permitido
                 * 11:30 → 12:30 = bloqueado
                 *
                 * Tópicos:
                 * - Levantamento e Especificação de Requisitos
                 * - Engenharia de Software
                 * - Arquitetura de Software
                 * - Programação Orientada a Objetos
                 */
                boolean horarioDisponivel = atendimentoCabeNaDisponibilidade(
                                dados.getHora(),

                                /*
                                 * O reagendamento não altera o serviço.
                                 * Portanto utilizamos a duração do
                                 * serviço já vinculado ao agendamento.
                                 */
                                agendamento
                                                .getServico()
                                                .getDuracaoMinutos(),

                                disponibilidades);

                if (!horarioDisponivel) {

                        throw new IllegalArgumentException(
                                        "O atendimento não cabe dentro do período de disponibilidade do profissional.");
                }

                /*
                 * -------------------------------------------------
                 * VERIFICA CONFLITO NO REAGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Precisamos comparar intervalos completos,
                 * exatamente como fazemos na criação.
                 *
                 * A diferença é que o próprio agendamento
                 * que está sendo reagendado deve ser ignorado
                 * durante a verificação.
                 *
                 * Tópicos:
                 * - Levantamento e Especificação de Requisitos
                 * - Engenharia de Software
                 * - Spring Data JPA
                 * - Banco de Dados Relacional
                 * - Programação Orientada a Objetos
                 */
                List<Agendamento> agendamentosExistentes = agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                agendamento
                                                                .getProfissional()
                                                                .getIdProfissional(),
                                                dados.getData(),
                                                StatusAgendamento.CANCELADO);

                /*
                 * A duração continua sendo a duração
                 * do serviço original do agendamento.
                 *
                 * O reagendamento altera somente data e hora.
                 */
                int duracaoServico = agendamento
                                .getServico()
                                .getDuracaoMinutos();

                /*
                 * Ignora o próprio agendamento e verifica
                 * se o novo intervalo entra em conflito
                 * com qualquer outro atendimento.
                 */
                boolean existeConflito = agendamentosExistentes
                                .stream()

                                /*
                                 * O agendamento que estamos alterando
                                 * não pode entrar em conflito consigo mesmo.
                                 */
                                .filter(outroAgendamento -> !outroAgendamento
                                                .getIdAgendamento()
                                                .equals(id))

                                /*
                                 * Verifica sobreposição real
                                 * entre os intervalos.
                                 */
                                .anyMatch(outroAgendamento ->

                                possuiSobreposicao(
                                                dados.getHora(),
                                                duracaoServico,
                                                outroAgendamento));

                if (existeConflito) {

                        throw new IllegalArgumentException(
                                        "O novo horário entra em conflito com outro agendamento do profissional.");
                }

                /*
                 * -------------------------------------------------
                 * 5. ALTERA DATA E HORÁRIO
                 * -------------------------------------------------
                 *
                 * Paciente, profissional e serviço permanecem
                 * os mesmos.
                 */
                agendamento.setData(dados.getData());
                agendamento.setHora(dados.getHora());

                /*
                 * -------------------------------------------------
                 * 6. SALVA O REAGENDAMENTO
                 * -------------------------------------------------
                 */
                Agendamento agendamentoReagendado = agendamentoRepository.save(agendamento);

                /*
                 * Retorna o agendamento atualizado
                 * utilizando o DTO de resposta.
                 */
                return converterParaDTO(agendamentoReagendado);
        }

        /*
         * Retorna somente os agendamentos relacionados
         * ao usuário atualmente autenticado.
         *
         * O comportamento depende do perfil presente
         * no token JWT:
         *
         * PACIENTE
         * → retorna os próprios agendamentos.
         *
         * PROFISSIONAL
         * → retorna os atendimentos atribuídos
         * ao próprio profissional.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Programação Orientada a Objetos
         * - Banco de Dados Relacional
         */
        public List<AgendamentoResponseDTO> listarMeusAgendamentos(
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * PACIENTE
                 * -------------------------------------------------
                 *
                 * Busca os agendamentos cujo paciente
                 * está relacionado ao usuário do JWT.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        return agendamentoRepository
                                        .findByPaciente_Usuario_IdUsuario(
                                                        idUsuarioAutenticado)
                                        .stream()
                                        .map(this::converterParaDTO)
                                        .toList();
                }

                /*
                 * -------------------------------------------------
                 * PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * Busca os agendamentos cujo profissional
                 * está relacionado ao usuário do JWT.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        return agendamentoRepository
                                        .findByProfissional_Usuario_IdUsuario(
                                                        idUsuarioAutenticado)
                                        .stream()
                                        .map(this::converterParaDTO)
                                        .toList();
                }

                /*
                 * O endpoint /me será destinado a
                 * PACIENTE e PROFISSIONAL.
                 *
                 * ADMINISTRADOR e RECEPCIONISTA terão acesso
                 * à relação geral através de /api/agendamentos.
                 */
                throw new IllegalArgumentException(
                                "Este perfil não possui consulta pessoal de agendamentos.");
        }

        /*
         * Cadastra um agendamento levando em consideração
         * o usuário e o perfil atualmente autenticados.
         *
         * Regras de autorização:
         *
         * PACIENTE
         * -> pode criar agendamento somente para si próprio.
         *
         * RECEPCIONISTA
         * -> pode criar para qualquer paciente.
         *
         * ADMINISTRADOR
         * -> pode criar para qualquer paciente.
         *
         * PROFISSIONAL
         * -> não realiza criação de agendamento.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public AgendamentoResponseDTO cadastrarComAutorizacao(
                        AgendamentoRequestDTO dados,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. PACIENTE
                 * -------------------------------------------------
                 *
                 * Se quem está fazendo a requisição é PACIENTE,
                 * precisamos descobrir qual Paciente está
                 * relacionado ao usuário presente no JWT.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        Paciente pacienteAutenticado = pacienteRepository
                                        .findByUsuario_IdUsuario(idUsuarioAutenticado)
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Paciente não encontrado para o usuário autenticado."));

                        /*
                         * Compara:
                         *
                         * paciente pertencente ao JWT
                         * X
                         * idPaciente enviado no JSON
                         *
                         * Se forem diferentes, a operação é bloqueada.
                         */
                        if (!pacienteAutenticado
                                        .getIdPaciente()
                                        .equals(dados.getIdPaciente())) {

                                throw new IllegalArgumentException(
                                                "O paciente só pode criar agendamentos para si próprio.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 2. PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * O profissional não possui permissão para
                 * criar um novo agendamento.
                 *
                 * Também faremos essa restrição posteriormente
                 * no SecurityConfig, mas mantemos a proteção
                 * na regra de negócio.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        throw new IllegalArgumentException(
                                        "O profissional não possui permissão para criar agendamentos.");
                }

                /*
                 * -------------------------------------------------
                 * 3. CADASTRA O AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Depois da autorização, reutilizamos o método
                 * que já implementa todas as regras de negócio:
                 *
                 * - paciente existe;
                 * - profissional existe;
                 * - serviço existe;
                 * - disponibilidade;
                 * - conflito de horário;
                 * - status AGENDADO.
                 */
                return cadastrar(dados);
        }

        /*
         * Converte o DayOfWeek padrão do Java
         * para o enum DiaSemana criado no projeto.
         *
         * Java:
         * MONDAY
         * TUESDAY
         * ...
         *
         * Projeto:
         * SEGUNDA
         * TERCA
         * ...
         */
        private DiaSemana converterDiaSemana(DayOfWeek dia) {

                return switch (dia) {

                        case MONDAY ->
                                DiaSemana.SEGUNDA;

                        case TUESDAY ->
                                DiaSemana.TERCA;

                        case WEDNESDAY ->
                                DiaSemana.QUARTA;

                        case THURSDAY ->
                                DiaSemana.QUINTA;

                        case FRIDAY ->
                                DiaSemana.SEXTA;

                        case SATURDAY ->
                                DiaSemana.SABADO;

                        /*
                         * Nosso modelo atual de disponibilidade
                         * não possui DOMINGO.
                         */
                        case SUNDAY ->
                                throw new IllegalArgumentException(
                                                "Não existe disponibilidade cadastrada para domingo.");
                };
        }

        /*
         * Converte a entidade Agendamento
         * para AgendamentoResponseDTO.
         *
         * Assim evitamos devolver diretamente
         * todas as entidades relacionadas.
         */
        private AgendamentoResponseDTO converterParaDTO(
                        Agendamento agendamento) {

                /*
                 * Recupera o nome do paciente
                 * através do Usuario relacionado.
                 */
                String nomePaciente = null;

                if (agendamento.getPaciente() != null
                                && agendamento.getPaciente().getUsuario() != null) {

                        nomePaciente = agendamento
                                        .getPaciente()
                                        .getUsuario()
                                        .getNome();
                }

                /*
                 * Recupera o nome do profissional
                 * através do Usuario relacionado.
                 */
                String nomeProfissional = null;

                if (agendamento.getProfissional() != null
                                && agendamento.getProfissional().getUsuario() != null) {

                        nomeProfissional = agendamento
                                        .getProfissional()
                                        .getUsuario()
                                        .getNome();
                }

                /*
                 * Monta o objeto que será devolvido pela API.
                 */
                return new AgendamentoResponseDTO(

                                agendamento.getIdAgendamento(),

                                agendamento.getPaciente()
                                                .getIdPaciente(),

                                nomePaciente,

                                agendamento.getProfissional()
                                                .getIdProfissional(),

                                nomeProfissional,

                                agendamento.getServico()
                                                .getIdServico(),

                                agendamento.getServico()
                                                .getNome(),

                                agendamento.getData(),

                                agendamento.getHora(),

                                agendamento.getStatus(),

                                agendamento.getObservacao(),

                                agendamento.getCriadoEm());
        }
}