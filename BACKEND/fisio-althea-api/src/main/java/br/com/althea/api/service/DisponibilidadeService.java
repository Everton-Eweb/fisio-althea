package br.com.althea.api.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.time.LocalTime;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.althea.api.dto.DisponibilidadeRequestDTO;
import br.com.althea.api.dto.DisponibilidadeResponseDTO;
import br.com.althea.api.dto.HorariosDisponiveisResponseDTO;

import br.com.althea.api.entity.Profissional;
import br.com.althea.api.entity.Servico;
import br.com.althea.api.entity.DiaSemana;
import br.com.althea.api.entity.Disponibilidade;
import br.com.althea.api.entity.Agendamento;
import br.com.althea.api.entity.StatusAgendamento;
import br.com.althea.api.entity.TipoPerfil;

import br.com.althea.api.repository.DisponibilidadeRepository;
import br.com.althea.api.repository.ProfissionalRepository;
import br.com.althea.api.repository.ServicoRepository;
import br.com.althea.api.repository.AgendamentoRepository;

/*
 * Camada de serviço responsável pelas operações
 * relacionadas à disponibilidade dos profissionais.
 *
 * Fluxo:
 *
 * Controller
 *      ↓
 * DisponibilidadeService
 *      ↓
 * DisponibilidadeRepository / ProfissionalRepository
 *      ↓
 * MySQL
 *
 * Tópicos:
 * - Arquitetura de Software
 * - Spring Framework
 * - Spring Data JPA
 * - Programação Orientada a Objetos
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Service
public class DisponibilidadeService {

        /*
         * Repository responsável pelo acesso
         * à tabela disponibilidade.
         */
        private final DisponibilidadeRepository disponibilidadeRepository;

        /*
         * Repository utilizado para verificar
         * se o profissional informado existe.
         */
        private final ProfissionalRepository profissionalRepository;

        /*
         * Utilizado para consultar o serviço escolhido
         * e descobrir sua duração em minutos.
         */
        private final ServicoRepository servicoRepository;

        /*
         * Utilizado para consultar os agendamentos
         * já existentes do profissional.
         */
        private final AgendamentoRepository agendamentoRepository;

        /*
         * Busca as faixas de disponibilidade ativas
         * de um profissional para uma determinada data.
         *
         * Exemplo:
         *
         * data = 02/09/2026
         * ↓
         * QUARTA-FEIRA
         * ↓
         * DiaSemana.QUARTA
         * ↓
         * busca as disponibilidades cadastradas
         * para aquele profissional na quarta-feira.
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Arquitetura de Software
         */
        private List<Disponibilidade> buscarFaixasDisponiveis(
                        Long idProfissional,
                        LocalDate data) {

                /*
                 * Converte o dia da semana do Java
                 * para o enum utilizado pela nossa aplicação.
                 */
                DiaSemana diaSemana = converterParaDiaSemana(data);

                /*
                 * Busca somente as disponibilidades
                 * que estão ativas.
                 */
                return disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                idProfissional,
                                                diaSemana);
        }

        /*
         * Gera os possíveis horários de início dentro
         * de uma faixa de disponibilidade.
         *
         * Exemplo:
         *
         * faixa:
         * 08:00 até 12:00
         *
         * duração:
         * 60 minutos
         *
         * resultado:
         * 08:00
         * 09:00
         * 10:00
         * 11:00
         *
         * O horário 12:00 não é incluído porque
         * um atendimento iniciado às 12:00 ultrapassaria
         * o limite da disponibilidade.
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Engenharia de Software
         */
        private List<LocalTime> gerarHorariosDaFaixa(
                        LocalTime horaInicio,
                        LocalTime horaFim,
                        int duracaoMinutos) {

                /*
                 * Evita duração inválida.
                 *
                 * Além de ser uma validação de segurança,
                 * isso impede que o while abaixo entre
                 * em um loop infinito.
                 */
                if (duracaoMinutos <= 0) {
                        throw new IllegalArgumentException(
                                        "A duração do serviço deve ser maior que zero.");
                }

                /*
                 * A faixa precisa possuir um início
                 * anterior ao horário final.
                 */
                if (!horaInicio.isBefore(horaFim)) {
                        throw new IllegalArgumentException(
                                        "A faixa de disponibilidade possui horários inválidos.");
                }

                /*
                 * Lista que armazenará os possíveis
                 * horários de início.
                 */
                List<LocalTime> horarios = new ArrayList<>();

                /*
                 * Começamos pelo início da disponibilidade.
                 */
                LocalTime horarioAtual = horaInicio;

                /*
                 * Só adicionamos um horário se o atendimento
                 * inteiro couber dentro da faixa disponível.
                 *
                 * Exemplo:
                 *
                 * horário atual = 11:30
                 * duração = 60 minutos
                 * fim da faixa = 12:00
                 *
                 * 11:30 + 60 = 12:30
                 *
                 * portanto 11:30 NÃO será oferecido.
                 */
                while (!horarioAtual
                                .plusMinutes(duracaoMinutos)
                                .isAfter(horaFim)) {

                        horarios.add(horarioAtual);

                        /*
                         * Avança exatamente a duração
                         * do serviço.
                         */
                        horarioAtual = horarioAtual.plusMinutes(
                                        duracaoMinutos);
                }

                return horarios;
        }

        /*
         * Gera todos os horários POSSÍVEIS a partir
         * das faixas de disponibilidade encontradas.
         *
         * Atenção:
         *
         * Neste momento ainda não removemos horários
         * que estejam ocupados por agendamentos.
         *
         * Portanto:
         *
         * POSSÍVEL ≠ necessariamente DISPONÍVEL
         *
         * Exemplo:
         *
         * Faixas:
         * 08:00 - 12:00
         * 14:00 - 18:00
         *
         * Serviço:
         * 60 minutos
         *
         * Resultado inicial:
         *
         * 08:00
         * 09:00
         * 10:00
         * 11:00
         * 14:00
         * 15:00
         * 16:00
         * 17:00
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         * - Engenharia de Software
         */
        private List<LocalTime> gerarHorariosPossiveis(
                        List<Disponibilidade> faixas,
                        int duracaoMinutos) {

                /*
                 * Lista que reunirá os horários
                 * provenientes de todas as faixas.
                 */
                List<LocalTime> horariosPossiveis = new ArrayList<>();

                /*
                 * Percorre cada faixa cadastrada
                 * para o profissional naquele dia.
                 */
                for (Disponibilidade faixa : faixas) {

                        /*
                         * Reaproveitamos o método criado
                         * anteriormente.
                         *
                         * Ele já garante que o atendimento
                         * inteiro caiba dentro da faixa.
                         */
                        List<LocalTime> horariosDaFaixa = gerarHorariosDaFaixa(
                                        faixa.getHoraInicio(),
                                        faixa.getHoraFim(),
                                        duracaoMinutos);

                        /*
                         * Adiciona os horários dessa faixa
                         * à lista geral.
                         */
                        horariosPossiveis.addAll(
                                        horariosDaFaixa);
                }

                /*
                 * Ordena os horários.
                 *
                 * Isso é útil caso existam várias
                 * faixas cadastradas no mesmo dia.
                 */
                horariosPossiveis.sort(
                                LocalTime::compareTo);

                return horariosPossiveis;
        }

        /*
         * Verifica se um novo atendimento possui
         * sobreposição com um agendamento já existente.
         *
         * Não basta comparar somente o horário inicial.
         * Precisamos comparar os intervalos completos.
         *
         * Exemplo:
         *
         * Novo atendimento:
         * 09:00 até 10:00
         *
         * Agendamento existente:
         * 09:30 até 10:30
         *
         * Existe sobreposição entre 09:30 e 10:00,
         * portanto o horário deve ser considerado ocupado.
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Engenharia de Software
         * - Modelagem de Dados
         */
        private boolean possuiSobreposicao(
                        LocalTime novoInicio,
                        int novaDuracaoMinutos,
                        Agendamento agendamentoExistente) {

                /*
                 * -------------------------------------------------
                 * 1. INTERVALO DO NOVO ATENDIMENTO
                 * -------------------------------------------------
                 */
                LocalTime novoFim = novoInicio.plusMinutes(
                                novaDuracaoMinutos);

                /*
                 * -------------------------------------------------
                 * 2. INTERVALO DO AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 *
                 * A duração não vem do novo serviço.
                 *
                 * Precisamos utilizar a duração do serviço
                 * que pertence ao agendamento já gravado.
                 */
                LocalTime existenteInicio = agendamentoExistente.getHora();

                int duracaoExistente = agendamentoExistente
                                .getServico()
                                .getDuracaoMinutos();

                LocalTime existenteFim = existenteInicio.plusMinutes(
                                duracaoExistente);

                /*
                 * -------------------------------------------------
                 * 3. VERIFICA A SOBREPOSIÇÃO
                 * -------------------------------------------------
                 *
                 * Dois intervalos se sobrepõem quando:
                 *
                 * novoInicio < existenteFim
                 *
                 * E
                 *
                 * novoFim > existenteInicio
                 *
                 * Usamos os horários finais como exclusivos.
                 *
                 * Portanto:
                 *
                 * existente: 09:00 - 10:00
                 * novo: 10:00 - 11:00
                 *
                 * NÃO existe conflito.
                 */
                return novoInicio.isBefore(existenteFim)
                                &&
                                novoFim.isAfter(existenteInicio);
        }

        /*
         * Remove da lista de horários possíveis aqueles
         * que possuem conflito com algum agendamento
         * já existente para o profissional naquela data.
         *
         * Agendamentos CANCELADOS não ocupam mais
         * a agenda e, portanto, são ignorados.
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Programação Orientada a Objetos
         * - Engenharia de Software
         */
        private List<LocalTime> filtrarHorariosOcupados(
                        Long idProfissional,
                        LocalDate data,
                        List<LocalTime> horariosPossiveis,
                        int duracaoNovoServico) {

                /*
                 * -------------------------------------------------
                 * 1. BUSCA OS AGENDAMENTOS QUE OCUPAM A AGENDA
                 * -------------------------------------------------
                 *
                 * O repository retorna todos os agendamentos
                 * daquele profissional e daquela data,
                 * exceto os CANCELADOS.
                 */
                List<Agendamento> agendamentosExistentes = agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                idProfissional,
                                                data,
                                                StatusAgendamento.CANCELADO);

                /*
                 * -------------------------------------------------
                 * 2. FILTRA OS HORÁRIOS
                 * -------------------------------------------------
                 *
                 * Um horário será mantido somente quando
                 * NÃO possuir sobreposição com nenhum
                 * agendamento existente.
                 */
                return horariosPossiveis
                                .stream()
                                .filter(horarioPossivel ->

                                agendamentosExistentes
                                                .stream()
                                                .noneMatch(agendamento ->

                                                possuiSobreposicao(
                                                                horarioPossivel,
                                                                duracaoNovoServico,
                                                                agendamento)))
                                .toList();
        }

        /*
         * Converte o DayOfWeek do Java
         * para o enum DiaSemana utilizado no sistema.
         *
         * Nossa aplicação trabalha atualmente
         * de segunda-feira até sábado.
         */
        private DiaSemana converterParaDiaSemana(LocalDate data) {

                return switch (data.getDayOfWeek()) {

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
                         * O enum atual não possui DOMINGO.
                         *
                         * Mantemos o mesmo comportamento usado
                         * nas regras de agendamento.
                         */
                        case SUNDAY ->
                                throw new IllegalArgumentException(
                                                "Não há disponibilidade cadastrada para domingo.");
                };
        }

        /*
         * Busca a duração, em minutos, do serviço
         * selecionado pelo usuário.
         *
         * Essa informação será usada posteriormente
         * para dividir as faixas de disponibilidade
         * em possíveis horários de atendimento.
         *
         * Exemplo:
         *
         * Serviço:
         * Fisioterapia
         * duração = 60 minutos
         *
         * Disponibilidade:
         * 08:00 até 12:00
         *
         * Possíveis horários:
         * 08:00
         * 09:00
         * 10:00
         * 11:00
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        private int buscarDuracaoServico(Long idServico) {

                /*
                 * Procura o serviço no banco de dados.
                 *
                 * Caso o ID informado não exista,
                 * interrompemos o cálculo dos horários.
                 */
                Servico servico = servicoRepository
                                .findById(idServico)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Serviço não encontrado."));

                /*
                 * Retorna a duração cadastrada
                 * para o serviço, em minutos.
                 */
                return servico.getDuracaoMinutos();
        }

        /*
         * Retorna os horários realmente disponíveis
         * de um profissional para uma determinada
         * data e serviço.
         *
         * Este método reúne todas as etapas necessárias
         * para atender ao RF05.
         *
         * Fluxo:
         *
         * 1. identifica a duração do serviço;
         * 2. localiza as faixas de disponibilidade;
         * 3. transforma as faixas em horários possíveis;
         * 4. remove horários que entram em conflito
         * com agendamentos já existentes;
         * 5. retorna o resultado em um DTO.
         *
         * Tópicos:
         * - Levantamento e Especificação de Requisitos
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - APIs REST
         */
        public HorariosDisponiveisResponseDTO buscarHorariosDisponiveis(
                        Long idProfissional,
                        Long idServico,
                        LocalDate data) {

                /*
                 * -------------------------------------------------
                 * 1. BUSCA A DURAÇÃO DO SERVIÇO
                 * -------------------------------------------------
                 *
                 * Exemplo:
                 *
                 * Fisioterapia = 60 minutos.
                 */
                int duracaoMinutos = buscarDuracaoServico(idServico);

                /*
                 * -------------------------------------------------
                 * 2. BUSCA AS FAIXAS DO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * A data é convertida internamente
                 * para DiaSemana.
                 *
                 * Exemplo:
                 *
                 * 02/09/2026
                 * ↓
                 * QUARTA
                 * ↓
                 * 08:00 - 12:00
                 */
                List<Disponibilidade> faixas = buscarFaixasDisponiveis(
                                idProfissional,
                                data);

                /*
                 * -------------------------------------------------
                 * 3. GERA OS HORÁRIOS POSSÍVEIS
                 * -------------------------------------------------
                 *
                 * Exemplo:
                 *
                 * faixa = 08:00 - 12:00
                 * duração = 60 minutos
                 *
                 * resultado:
                 *
                 * 08:00
                 * 09:00
                 * 10:00
                 * 11:00
                 */
                List<LocalTime> horariosPossiveis = gerarHorariosPossiveis(
                                faixas,
                                duracaoMinutos);

                /*
                 * -------------------------------------------------
                 * 4. REMOVE OS HORÁRIOS OCUPADOS
                 * -------------------------------------------------
                 *
                 * Aqui verificamos os agendamentos existentes
                 * e retiramos qualquer horário que tenha
                 * sobreposição.
                 */
                List<LocalTime> horariosDisponiveis = filtrarHorariosOcupados(
                                idProfissional,
                                data,
                                horariosPossiveis,
                                duracaoMinutos);

                /*
                 * -------------------------------------------------
                 * 5. MONTA A RESPOSTA
                 * -------------------------------------------------
                 */
                return new HorariosDisponiveisResponseDTO(
                                idProfissional,
                                idServico,
                                data,
                                horariosDisponiveis);
        }

        /*
         * Injeção de dependência pelo construtor.
         *
         * O Spring fornece automaticamente
         * as implementações dos repositories.
         */
        public DisponibilidadeService(
                        DisponibilidadeRepository disponibilidadeRepository,
                        ProfissionalRepository profissionalRepository,
                        ServicoRepository servicoRepository,
                        AgendamentoRepository agendamentoRepository) {

                this.disponibilidadeRepository = disponibilidadeRepository;
                this.profissionalRepository = profissionalRepository;
                this.servicoRepository = servicoRepository;
                this.agendamentoRepository = agendamentoRepository;
        }

        /*
         * Retorna todas as disponibilidades cadastradas.
         *
         * Cada entidade é convertida para DTO antes
         * de ser devolvida para a API.
         */
        public List<DisponibilidadeResponseDTO> listarTodas() {

                return disponibilidadeRepository.findAll()
                                .stream()
                                .map(this::converterParaDTO)
                                .toList();
        }

        /*
         * Busca uma disponibilidade pelo seu ID.
         *
         * Optional é utilizado porque o registro
         * pode existir ou não.
         */
        public Optional<DisponibilidadeResponseDTO> buscarPorId(Long id) {

                return disponibilidadeRepository.findById(id)
                                .map(this::converterParaDTO);
        }

        /*
         * Cadastra uma disponibilidade levando em consideração
         * o usuário e o perfil atualmente autenticados.
         *
         * Essa versão será utilizada pelo Controller
         * quando integrarmos os dados do JWT.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public DisponibilidadeResponseDTO cadastrarComAutorizacao(
                        DisponibilidadeRequestDTO dados,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O PROFISSIONAL INFORMADO
                 * -------------------------------------------------
                 */
                Profissional profissional = profissionalRepository
                                .findById(dados.getIdProfissional())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Profissional não encontrado."));

                /*
                 * -------------------------------------------------
                 * 2. VALIDA A PROPRIEDADE DO RECURSO
                 * -------------------------------------------------
                 *
                 * ADMINISTRADOR e RECEPCIONISTA podem cadastrar
                 * disponibilidade para qualquer profissional.
                 *
                 * PROFISSIONAL só pode cadastrar para ele próprio.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        /*
                         * Recupera o usuário vinculado
                         * ao profissional informado no JSON.
                         */
                        Long idUsuarioDoProfissional = profissional
                                        .getUsuario()
                                        .getIdUsuario();

                        /*
                         * Compara:
                         *
                         * usuário do JWT
                         * X
                         * usuário vinculado ao idProfissional enviado
                         */
                        if (!idUsuarioAutenticado.equals(idUsuarioDoProfissional)) {

                                throw new IllegalArgumentException(
                                                "O profissional só pode cadastrar a própria disponibilidade.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 3. CRIA A DISPONIBILIDADE
                 * -------------------------------------------------
                 */
                Disponibilidade disponibilidade = new Disponibilidade();

                disponibilidade.setProfissional(profissional);
                disponibilidade.setDiaSemana(dados.getDiaSemana());
                disponibilidade.setHoraInicio(dados.getHoraInicio());
                disponibilidade.setHoraFim(dados.getHoraFim());

                /*
                 * Toda nova disponibilidade começa ativa.
                 */
                disponibilidade.setAtivo(true);

                /*
                 * -------------------------------------------------
                 * 4. SALVA NO BANCO
                 * -------------------------------------------------
                 */
                Disponibilidade disponibilidadeSalva = disponibilidadeRepository.save(disponibilidade);

                /*
                 * Retorna o DTO de resposta.
                 */
                return converterParaDTO(disponibilidadeSalva);
        }

        /*
         * Cadastra uma nova disponibilidade.
         *
         * Antes de salvar, verificamos se o profissional
         * informado realmente existe no banco.
         */
        @Transactional
        public DisponibilidadeResponseDTO cadastrar(
                        DisponibilidadeRequestDTO dados) {

                /*
                 * Busca o profissional pelo ID recebido.
                 *
                 * Caso não exista, interrompemos a operação.
                 */
                Profissional profissional = profissionalRepository
                                .findById(dados.getIdProfissional())
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Profissional não encontrado."));

                /*
                 * Cria o objeto Disponibilidade.
                 */
                Disponibilidade disponibilidade = new Disponibilidade();

                /*
                 * Relaciona a disponibilidade ao profissional.
                 *
                 * No banco, essa relação será representada
                 * pela coluna id_profissional.
                 */
                disponibilidade.setProfissional(profissional);

                /*
                 * Preenche os horários e o dia da semana
                 * recebidos pelo DTO.
                 */
                disponibilidade.setDiaSemana(dados.getDiaSemana());
                disponibilidade.setHoraInicio(dados.getHoraInicio());
                disponibilidade.setHoraFim(dados.getHoraFim());

                /*
                 * Toda nova disponibilidade começa ativa.
                 *
                 * Não deixamos esse valor ser escolhido
                 * pelo cliente da API.
                 */
                disponibilidade.setAtivo(true);

                /*
                 * Salva a disponibilidade no banco.
                 */
                Disponibilidade disponibilidadeSalva = disponibilidadeRepository.save(disponibilidade);

                /*
                 * Converte a entidade salva para DTO
                 * antes de devolver a resposta.
                 */
                return converterParaDTO(disponibilidadeSalva);
        }

        /*
         * Converte a entidade Disponibilidade
         * para DisponibilidadeResponseDTO.
         */
        private DisponibilidadeResponseDTO converterParaDTO(
                        Disponibilidade disponibilidade) {

                /*
                 * Recupera somente o ID do profissional
                 * relacionado.
                 *
                 * Não devolvemos toda a entidade Profissional,
                 * evitando trazer também os dados do Usuario.
                 */
                Long idProfissional = null;

                if (disponibilidade.getProfissional() != null) {
                        idProfissional = disponibilidade
                                        .getProfissional()
                                        .getIdProfissional();
                }

                /*
                 * Monta o DTO que será enviado pela API.
                 */
                return new DisponibilidadeResponseDTO(
                                disponibilidade.getIdDisponibilidade(),
                                idProfissional,
                                disponibilidade.getDiaSemana(),
                                disponibilidade.getHoraInicio(),
                                disponibilidade.getHoraFim(),
                                disponibilidade.getAtivo());
        }
}