package br.com.althea.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.althea.api.dto.AgendamentoRequestDTO;
import br.com.althea.api.dto.AgendamentoResponseDTO;
import br.com.althea.api.dto.ReagendamentoRequestDTO;

import br.com.althea.api.entity.Agendamento;
import br.com.althea.api.entity.DiaSemana;
import br.com.althea.api.entity.Disponibilidade;
import br.com.althea.api.entity.Paciente;
import br.com.althea.api.entity.Profissional;
import br.com.althea.api.entity.Servico;
import br.com.althea.api.entity.Usuario;
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
 * Testes unitários das regras de agendamento.
 *
 * Utilizamos Mockito para simular os repositories.
 * Dessa forma, este teste NÃO altera o banco MySQL.
 *
 * Tópicos:
 * - Testes de Software
 * - Engenharia de Software
 * - Arquitetura de Software
 * - Programação Orientada a Objetos
 */
@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

        /*
         * Dependências externas da Service.
         *
         * @Mock cria versões simuladas dessas classes.
         */
        @Mock
        private AgendamentoRepository agendamentoRepository;

        @Mock
        private PacienteRepository pacienteRepository;

        @Mock
        private ProfissionalRepository profissionalRepository;

        @Mock
        private ServicoRepository servicoRepository;

        @Mock
        private DisponibilidadeRepository disponibilidadeRepository;

        @Mock
        private EntityManager entityManager;

        /*
         * O Mockito cria o AgendamentoService
         * utilizando os mocks acima.
         */
        @InjectMocks
        private AgendamentoService agendamentoService;

        /*
         * RN01
         *
         * Deve impedir um novo atendimento
         * quando houver sobreposição com outro
         * agendamento do mesmo profissional.
         */
        @Test
        void deveBloquearAgendamentoComSobreposicaoDeHorario() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DA NOVA SOLICITAÇÃO
                 * -------------------------------------------------
                 *
                 * Tentativa:
                 * 09:30 até 10:30.
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                dados.setIdPaciente(4L);
                dados.setIdProfissional(3L);
                dados.setIdServico(1L);
                dados.setData(data);
                dados.setHora(
                                LocalTime.of(9, 30));

                /*
                 * -------------------------------------------------
                 * 2. ENTIDADES SIMULADAS
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);
                /*
                 * Este mock representa o profissional 3.
                 */
                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                Servico novoServico = org.mockito.Mockito.mock(
                                Servico.class);

                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                /*
                 * Serviço novo dura 60 minutos.
                 *
                 * Portanto:
                 * 09:30 → 10:30.
                 */
                when(novoServico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * O profissional trabalha na terça:
                 *
                 * 08:00 → 12:00.
                 */
                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 3. AGENDAMENTO JÁ EXISTENTE
                 * -------------------------------------------------
                 *
                 * Existente:
                 * 09:00 → 10:00.
                 */
                Agendamento existente = org.mockito.Mockito.mock(
                                Agendamento.class);

                Servico servicoExistente = org.mockito.Mockito.mock(
                                Servico.class);

                when(existente.getHora())
                                .thenReturn(
                                                LocalTime.of(9, 0));

                when(existente.getServico())
                                .thenReturn(
                                                servicoExistente);

                when(servicoExistente.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 4. COMPORTAMENTO DOS REPOSITORIES
                 * -------------------------------------------------
                 */
                when(pacienteRepository.findById(4L))
                                .thenReturn(
                                                Optional.of(paciente));

                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(novoServico));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of(existente));

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA E VERIFICA A REGRA
                 * -------------------------------------------------
                 *
                 * Esperamos que a Service lance uma exceção
                 * porque 09:30–10:30 se sobrepõe a 09:00–10:00.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .cadastrar(dados));

                /*
                 * Também confirmamos a mensagem
                 * produzida pela regra.
                 */
                assertEquals(
                                "O horário informado entra em conflito com outro agendamento do profissional.",
                                excecao.getMessage());

                /*
                 * Como houve conflito, nenhum INSERT
                 * deve chegar ao Repository.
                 */
                verify(
                                agendamentoRepository,
                                never()).saveAndFlush(any());
        }

        /*
         * RN02
         *
         * Deve impedir um agendamento quando
         * o atendimento ultrapassar o horário final
         * da disponibilidade do profissional.
         *
         * Exemplo:
         *
         * disponibilidade:
         * 08:00 até 12:00
         *
         * serviço:
         * 60 minutos
         *
         * tentativa:
         * 11:30 até 12:30
         *
         * Resultado esperado:
         * agendamento bloqueado.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Arquitetura de Software
         */
        @Test
        void deveBloquearAgendamentoQueUltrapassaDisponibilidade() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DO NOVO AGENDAMENTO
                 * -------------------------------------------------
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                dados.setIdPaciente(4L);
                dados.setIdProfissional(3L);
                dados.setIdServico(1L);
                dados.setData(data);

                /*
                 * Tentativa:
                 *
                 * 11:30 + 60 minutos = 12:30.
                 */
                dados.setHora(
                                LocalTime.of(11, 30));

                /*
                 * -------------------------------------------------
                 * 2. ENTIDADES SIMULADAS
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                /*
                 * O mock representa o profissional 3.
                 */
                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                /*
                 * Serviço com duração de 60 minutos.
                 */
                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * Profissional disponível somente:
                 *
                 * 08:00 até 12:00.
                 */
                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 3. COMPORTAMENTO DOS REPOSITORIES
                 * -------------------------------------------------
                 */
                when(pacienteRepository.findById(4L))
                                .thenReturn(
                                                Optional.of(paciente));

                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(servico));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * -------------------------------------------------
                 * 4. EXECUTA A REGRA
                 * -------------------------------------------------
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .cadastrar(dados));

                /*
                 * Confirma que a RN02 foi responsável
                 * pelo bloqueio.
                 */
                assertEquals(
                                "O atendimento não cabe dentro do período de disponibilidade do profissional.",
                                excecao.getMessage());

                /*
                 * Como a disponibilidade já invalidou
                 * o agendamento, nada deve ser salvo.
                 */
                verify(
                                agendamentoRepository,
                                never()).saveAndFlush(any());
        }

        /*
         * Cenário positivo
         *
         * Deve permitir o agendamento quando:
         *
         * - o profissional possui disponibilidade;
         * - o atendimento cabe totalmente na faixa;
         * - não existe conflito com outro agendamento.
         *
         * Tópicos:
         * - Testes de Software
         * - Engenharia de Software
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @Test
        void devePermitirAgendamentoQuandoHorarioEstiverLivre() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DO AGENDAMENTO
                 * -------------------------------------------------
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                dados.setIdPaciente(4L);
                dados.setIdProfissional(3L);
                dados.setIdServico(1L);
                dados.setData(data);
                dados.setHora(
                                LocalTime.of(11, 0));

                dados.setObservacao(
                                "Teste automatizado de horário livre");

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * -------------------------------------------------
                 * 4. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 5. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * 08:00 até 12:00.
                 *
                 * O atendimento das 11:00 às 12:00
                 * cabe exatamente na faixa.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 6. REPOSITORIES
                 * -------------------------------------------------
                 */
                when(pacienteRepository.findById(4L))
                                .thenReturn(
                                                Optional.of(paciente));

                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(servico));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * Não existe nenhum outro agendamento
                 * ocupando a agenda nessa data.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of());

                /*
                 * Quando a Service salvar o agendamento,
                 * devolvemos o próprio objeto recebido.
                 */
                when(agendamentoRepository
                                .saveAndFlush(any(Agendamento.class)))
                                .thenAnswer(invocacao -> invocacao.getArgument(0));

                /*
                 * -------------------------------------------------
                 * 7. EXECUTA
                 * -------------------------------------------------
                 *
                 * Se a regra estiver correta,
                 * nenhuma exceção será lançada.
                 */
                agendamentoService.cadastrar(dados);

                /*
                 * Confirma que o agendamento realmente
                 * chegou até a etapa de persistência.
                 */
                verify(
                                agendamentoRepository).saveAndFlush(
                                                any(Agendamento.class));
        }

        /*
         * RN03
         *
         * Deve alterar o status de um agendamento
         * para CANCELADO.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Programação Orientada a Objetos
         */
        @Test
        void deveCancelarAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. CRIA O AGENDAMENTO SIMULADO
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                /*
                 * O método cancelar() devolve um DTO.
                 *
                 * Portanto precisamos simular também
                 * os relacionamentos utilizados pelo
                 * converterParaDTO().
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                /*
                 * -------------------------------------------------
                 * 2. RELACIONAMENTOS DO AGENDAMENTO
                 * -------------------------------------------------
                 */
                when(agendamento.getPaciente())
                                .thenReturn(paciente);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                when(agendamento.getProfissional())
                                .thenReturn(profissional);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                when(agendamento.getServico())
                                .thenReturn(servico);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * -------------------------------------------------
                 * 3. REPOSITORY
                 * -------------------------------------------------
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * A Service salva e depois converte
                 * o mesmo objeto para DTO.
                 */
                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 4. EXECUTA O CANCELAMENTO
                 * -------------------------------------------------
                 */
                agendamentoService.cancelar(7L);

                /*
                 * -------------------------------------------------
                 * 5. VERIFICA A RN03
                 * -------------------------------------------------
                 *
                 * Confirma que o status foi alterado
                 * para CANCELADO.
                 */
                verify(agendamento)
                                .setStatus(
                                                StatusAgendamento.CANCELADO);

                /*
                 * Confirma que a alteração
                 * foi enviada ao Repository.
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RN03
         *
         * Deve permitir um novo agendamento quando
         * o horário foi liberado por um cancelamento.
         *
         * A consulta utilizada pela Service ignora
         * agendamentos com status CANCELADO.
         *
         * Portanto, se não houver outro atendimento
         * ativo naquele intervalo, o horário pode
         * ser utilizado novamente.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Spring Data JPA
         * - Arquitetura de Software
         */
        @Test
        void devePermitirAgendamentoEmHorarioLiberadoPorCancelamento() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DO NOVO AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Vamos tentar agendar às 09:00.
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                dados.setIdPaciente(4L);
                dados.setIdProfissional(3L);
                dados.setIdServico(1L);
                dados.setData(data);
                dados.setHora(
                                LocalTime.of(9, 0));

                dados.setObservacao(
                                "Teste de horário liberado após cancelamento");

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * -------------------------------------------------
                 * 4. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 5. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * Profissional disponível:
                 * 08:00 até 12:00.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 6. REPOSITORIES
                 * -------------------------------------------------
                 */
                when(pacienteRepository.findById(4L))
                                .thenReturn(
                                                Optional.of(paciente));

                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(servico));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * A consulta procura somente agendamentos
                 * cujo status seja diferente de CANCELADO.
                 *
                 * Como o antigo atendimento foi cancelado,
                 * ele não aparece nesta lista.
                 *
                 * Resultado:
                 * horário novamente livre.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of());

                /*
                 * Simula o salvamento do novo agendamento.
                 */
                when(agendamentoRepository
                                .saveAndFlush(any(Agendamento.class)))
                                .thenAnswer(invocacao -> invocacao.getArgument(0));

                /*
                 * -------------------------------------------------
                 * 7. EXECUTA
                 * -------------------------------------------------
                 */
                agendamentoService.cadastrar(dados);

                /*
                 * -------------------------------------------------
                 * 8. VERIFICA
                 * -------------------------------------------------
                 *
                 * Se o horário foi realmente liberado,
                 * a Service deve chegar até o salvamento.
                 */
                verify(
                                agendamentoRepository).saveAndFlush(
                                                any(Agendamento.class));
        }

        /*
         * RN04
         *
         * Deve impedir o reagendamento quando
         * o novo horário entrar em conflito
         * com outro atendimento do profissional.
         *
         * Exemplo:
         *
         * outro agendamento:
         * 09:00 até 10:00
         *
         * tentativa:
         * 09:30 até 10:30
         *
         * Resultado:
         * reagendamento bloqueado.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @Test
        void deveBloquearReagendamentoComSobreposicaoDeHorario() {

                /*
                 * -------------------------------------------------
                 * 1. NOVO HORÁRIO SOLICITADO
                 * -------------------------------------------------
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(data);

                /*
                 * Tentativa:
                 * 09:30 até 10:30.
                 */
                dados.setHora(
                                LocalTime.of(9, 30));

                /*
                 * -------------------------------------------------
                 * 2. AGENDAMENTO QUE SERÁ REAGENDADO
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(agendamento.getProfissional())
                                .thenReturn(profissional);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(agendamento.getServico())
                                .thenReturn(servico);

                /*
                 * O serviço dura 60 minutos.
                 */
                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 3. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * 08:00 até 12:00.
                 *
                 * Portanto 09:30 está dentro da faixa,
                 * permitindo que o teste avance até
                 * a verificação de conflito.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 4. OUTRO AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 *
                 * Outro atendimento:
                 * 09:00 até 10:00.
                 */
                Agendamento outroAgendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Servico outroServico = org.mockito.Mockito.mock(
                                Servico.class);

                /*
                 * ID diferente do agendamento 7,
                 * pois o próprio registro que está sendo
                 * reagendado deve ser ignorado.
                 */
                when(outroAgendamento.getIdAgendamento())
                                .thenReturn(8L);

                when(outroAgendamento.getHora())
                                .thenReturn(
                                                LocalTime.of(9, 0));

                when(outroAgendamento.getServico())
                                .thenReturn(outroServico);

                when(outroServico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 5. REPOSITORIES
                 * -------------------------------------------------
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of(outroAgendamento));

                /*
                 * -------------------------------------------------
                 * 6. EXECUTA E VERIFICA A RN04
                 * -------------------------------------------------
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .reagendar(
                                                                7L,
                                                                dados));

                /*
                 * Confirma que o bloqueio ocorreu
                 * especificamente pelo conflito de horário.
                 */
                assertEquals(
                                "O novo horário entra em conflito com outro agendamento do profissional.",
                                excecao.getMessage());

                /*
                 * Como houve conflito, nenhuma alteração
                 * deve ser persistida.
                 */
                verify(
                                agendamentoRepository,
                                never()).save(any(Agendamento.class));
        }

        /*
         * RN04 - cenário positivo
         *
         * Deve permitir o reagendamento quando
         * o novo horário estiver disponível e
         * não possuir conflito com outros atendimentos.
         *
         * Exemplo:
         *
         * disponibilidade:
         * 08:00 até 12:00
         *
         * novo horário:
         * 10:00 até 11:00
         *
         * conflitos:
         * nenhum
         *
         * Resultado:
         * reagendamento permitido.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Arquitetura de Software
         */
        @Test
        void devePermitirReagendamentoParaHorarioLivre() {

                /*
                 * -------------------------------------------------
                 * 1. NOVA DATA E HORÁRIO
                 * -------------------------------------------------
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(data);

                /*
                 * Novo atendimento:
                 * 10:00 até 11:00.
                 */
                dados.setHora(
                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 2. AGENDAMENTO ATUAL
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                /*
                 * -------------------------------------------------
                 * 3. PACIENTE
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(agendamento.getPaciente())
                                .thenReturn(paciente);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 4. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(agendamento.getProfissional())
                                .thenReturn(profissional);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * -------------------------------------------------
                 * 5. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(agendamento.getServico())
                                .thenReturn(servico);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 6. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * Profissional disponível:
                 * 08:00 até 12:00.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 7. REPOSITORIES
                 * -------------------------------------------------
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * Nenhum outro atendimento está
                 * ocupando o novo horário.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of());

                /*
                 * Simula a atualização no banco.
                 */
                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 8. EXECUTA O REAGENDAMENTO
                 * -------------------------------------------------
                 */
                agendamentoService.reagendar(
                                7L,
                                dados);

                /*
                 * -------------------------------------------------
                 * 9. VERIFICA AS ALTERAÇÕES
                 * -------------------------------------------------
                 */
                verify(agendamento)
                                .setData(data);

                verify(agendamento)
                                .setHora(
                                                LocalTime.of(10, 0));

                /*
                 * Confirma que a alteração chegou
                 * até a persistência.
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RF07 - cenário positivo.
         *
         * Deve permitir confirmar um agendamento existente,
         * alterando seu status de AGENDADO para CONFIRMADO.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveConfirmarAgendamentoExistente() {

                /*
                 * -------------------------------------------------
                 * 1. PREPARA O AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Criamos um agendamento que inicialmente
                 * está com status AGENDADO.
                 */
                Agendamento agendamento = new Agendamento();

                agendamento.setStatus(
                                StatusAgendamento.AGENDADO);

                /*
                 * -------------------------------------------------
                 * 2. PREPARA O PACIENTE
                 * -------------------------------------------------
                 *
                 * O converterParaDTO() acessa:
                 *
                 * paciente.getIdPaciente()
                 * paciente.getUsuario().getNome()
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Everton");

                /*
                 * -------------------------------------------------
                 * 3. PREPARA O PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Carlos");

                /*
                 * -------------------------------------------------
                 * 4. PREPARA O SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * -------------------------------------------------
                 * 5. MONTA OS RELACIONAMENTOS
                 * -------------------------------------------------
                 */
                agendamento.setPaciente(paciente);
                agendamento.setProfissional(profissional);
                agendamento.setServico(servico);

                /*
                 * -------------------------------------------------
                 * 6. SIMULA A BUSCA NO BANCO
                 * -------------------------------------------------
                 *
                 * Informamos ao Mockito que o agendamento
                 * de ID 7 existe.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 7. SIMULA O SALVAMENTO
                 * -------------------------------------------------
                 *
                 * O Repository devolve o mesmo objeto
                 * depois da alteração.
                 */
                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 8. EXECUTA A CONFIRMAÇÃO
                 * -------------------------------------------------
                 */
                AgendamentoResponseDTO resposta = agendamentoService.confirmar(7L);

                /*
                 * -------------------------------------------------
                 * 9. VERIFICA O RESULTADO
                 * -------------------------------------------------
                 *
                 * O principal comportamento do RF07:
                 *
                 * AGENDADO -> CONFIRMADO
                 */
                assertEquals(
                                StatusAgendamento.CONFIRMADO,
                                agendamento.getStatus());

                /*
                 * A Service deve devolver um DTO.
                 */
                assertNotNull(resposta);

                /*
                 * -------------------------------------------------
                 * 10. CONFIRMA O SALVAMENTO
                 * -------------------------------------------------
                 *
                 * O mesmo agendamento encontrado deve ser
                 * enviado ao Repository depois da mudança.
                 */
                verify(agendamentoRepository)
                                .save(agendamento);

                /*
                 * Garante que o objeto salvo é realmente
                 * o mesmo que foi recuperado anteriormente.
                 */
                assertSame(
                                agendamento,
                                agendamentoRepository
                                                .findById(7L)
                                                .orElseThrow());
        }

        /*
         * RF07 - cenário negativo.
         *
         * Não deve permitir confirmar um agendamento
         * que não existe no sistema.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveBloquearConfirmacaoDeAgendamentoInexistente() {

                /*
                 * -------------------------------------------------
                 * 1. SIMULA AGENDAMENTO INEXISTENTE
                 * -------------------------------------------------
                 *
                 * Optional.empty() representa a situação em que
                 * nenhum agendamento foi encontrado para o ID.
                 */
                when(agendamentoRepository.findById(999L))
                                .thenReturn(Optional.empty());

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA E ESPERA A EXCEÇÃO
                 * -------------------------------------------------
                 */
                RecursoNaoEncontradoException excecao = assertThrows(
                                RecursoNaoEncontradoException.class,
                                () -> agendamentoService.confirmar(999L));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A MENSAGEM
                 * -------------------------------------------------
                 */
                assertEquals(
                                "Agendamento não encontrado.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Como o agendamento não existe, o método
                 * save() nunca deve ser chamado.
                 */
                verify(
                                agendamentoRepository,
                                never()).save(
                                                org.mockito.ArgumentMatchers
                                                                .any(Agendamento.class));
        }

        /*
         * RF08 - cenário negativo.
         *
         * Não deve permitir cancelar um agendamento
         * que não existe no sistema.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveBloquearCancelamentoDeAgendamentoInexistente() {

                /*
                 * -------------------------------------------------
                 * 1. SIMULA AGENDAMENTO INEXISTENTE
                 * -------------------------------------------------
                 *
                 * O Repository informa que não encontrou
                 * nenhum agendamento para o ID 999.
                 */
                when(agendamentoRepository.findById(999L))
                                .thenReturn(Optional.empty());

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA O CANCELAMENTO
                 * -------------------------------------------------
                 *
                 * Como o agendamento não existe, esperamos
                 * uma IllegalArgumentException.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService.cancelar(999L));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A MENSAGEM
                 * -------------------------------------------------
                 */
                assertEquals(
                                "Agendamento não encontrado.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Se o agendamento não foi encontrado,
                 * o Repository não pode executar save().
                 */
                verify(
                                agendamentoRepository,
                                never()).save(
                                                org.mockito.ArgumentMatchers.any(Agendamento.class));
        }

        /*
         * RF08 - cenário negativo.
         *
         * Não deve permitir reagendar um agendamento
         * que não existe no sistema.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveBloquearReagendamentoDeAgendamentoInexistente() {

                /*
                 * -------------------------------------------------
                 * 1. PREPARA OS NOVOS DADOS
                 * -------------------------------------------------
                 *
                 * Mesmo existindo uma nova data e um novo horário,
                 * o reagendamento só pode acontecer se o
                 * agendamento original existir.
                 */
                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(
                                LocalDate.of(2026, 9, 8));

                dados.setHora(
                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 2. SIMULA AGENDAMENTO INEXISTENTE
                 * -------------------------------------------------
                 *
                 * O Repository não encontra o ID informado.
                 */
                when(agendamentoRepository.findById(999L))
                                .thenReturn(Optional.empty());

                /*
                 * -------------------------------------------------
                 * 3. EXECUTA O REAGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Como o agendamento não existe,
                 * esperamos uma IllegalArgumentException.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .reagendar(999L, dados));

                /*
                 * -------------------------------------------------
                 * 4. CONFERE A MENSAGEM
                 * -------------------------------------------------
                 */
                assertEquals(
                                "Agendamento não encontrado.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 5. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Como o agendamento original não existe,
                 * nenhuma alteração deve chegar ao save().
                 */
                verify(
                                agendamentoRepository,
                                never()).save(
                                                org.mockito.ArgumentMatchers
                                                                .any(Agendamento.class));
        }

        /*
         * RF06 - autorização por propriedade.
         *
         * Um paciente autenticado não pode criar
         * um agendamento para outro paciente.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Arquitetura de Software
         */
        @Test
        void deveBloquearPacienteAoCriarAgendamentoParaOutroPaciente() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS RECEBIDOS
                 * -------------------------------------------------
                 *
                 * O JSON tenta criar um agendamento
                 * para o paciente ID 5.
                 */
                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                dados.setIdPaciente(5L);

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE AUTENTICADO
                 * -------------------------------------------------
                 *
                 * O usuário autenticado está relacionado
                 * ao paciente ID 4.
                 */
                Paciente pacienteAutenticado = org.mockito.Mockito.mock(
                                Paciente.class);

                when(pacienteAutenticado.getIdPaciente())
                                .thenReturn(4L);

                /*
                 * Simula a busca através do idUsuario
                 * recuperado do JWT.
                 */
                when(pacienteRepository
                                .findByUsuario_IdUsuario(6L))
                                .thenReturn(
                                                Optional.of(pacienteAutenticado));

                /*
                 * -------------------------------------------------
                 * 3. EXECUTA
                 * -------------------------------------------------
                 *
                 * Temos:
                 *
                 * usuário autenticado → paciente 4
                 * JSON → paciente 5
                 *
                 * Portanto, a operação deve ser bloqueada.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .cadastrarComAutorizacao(
                                                                dados,
                                                                6L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 4. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O paciente só pode criar agendamentos para si próprio.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 5. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou antes das
                 * regras de agendamento, nenhum registro
                 * deve chegar ao banco.
                 */
                verify(
                                agendamentoRepository,
                                never()).saveAndFlush(
                                                any(Agendamento.class));
        }

        /*
         * RF06 - autorização por perfil.
         *
         * Um usuário com perfil PROFISSIONAL
         * não pode criar novos agendamentos.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         */
        @Test
        void deveBloquearProfissionalAoCriarAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. PREPARA OS DADOS
                 * -------------------------------------------------
                 *
                 * Neste cenário não precisamos preencher todos
                 * os campos porque a autorização deve bloquear
                 * a operação antes da criação do agendamento.
                 */
                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA
                 * -------------------------------------------------
                 *
                 * Simulamos um usuário autenticado
                 * com perfil PROFISSIONAL.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .cadastrarComAutorizacao(
                                                                dados,
                                                                8L,
                                                                TipoPerfil.PROFISSIONAL));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O profissional não possui permissão para criar agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 */
                verify(
                                agendamentoRepository,
                                never()).saveAndFlush(
                                                any(Agendamento.class));
        }

        /*
         * RF06 - cenário positivo de autorização.
         *
         * Um paciente autenticado deve conseguir criar
         * um agendamento para ele próprio.
         *
         * Depois da autorização, continuam sendo aplicadas
         * normalmente as regras de disponibilidade e conflito.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void devePermitirPacienteCriarAgendamentoParaSiProprio() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DO AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * O paciente autenticado será o paciente ID 4.
                 * O próprio JSON também informa o paciente ID 4.
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                AgendamentoRequestDTO dados = new AgendamentoRequestDTO();

                dados.setIdPaciente(4L);
                dados.setIdProfissional(3L);
                dados.setIdServico(1L);
                dados.setData(data);

                /*
                 * Atendimento:
                 * 11:00 até 12:00.
                 */
                dados.setHora(
                                LocalTime.of(11, 0));

                dados.setObservacao(
                                "Agendamento criado pelo próprio paciente");

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE AUTENTICADO
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * O usuário ID 6, obtido do JWT,
                 * pertence ao paciente ID 4.
                 *
                 * Essa consulta é realizada primeiro pelo
                 * cadastrarComAutorizacao().
                 */
                when(pacienteRepository
                                .findByUsuario_IdUsuario(6L))
                                .thenReturn(
                                                Optional.of(paciente));

                /*
                 * Depois da autorização, cadastrar()
                 * busca novamente o paciente pelo ID.
                 */
                when(pacienteRepository.findById(4L))
                                .thenReturn(
                                                Optional.of(paciente));

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                /*
                 * -------------------------------------------------
                 * 4. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(servico));

                /*
                 * -------------------------------------------------
                 * 5. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * O profissional atende:
                 * 08:00 até 12:00.
                 *
                 * Portanto:
                 * 11:00 até 12:00 cabe completamente
                 * dentro da disponibilidade.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * -------------------------------------------------
                 * 6. NÃO EXISTE CONFLITO
                 * -------------------------------------------------
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of());

                /*
                 * -------------------------------------------------
                 * 7. SIMULA A PERSISTÊNCIA
                 * -------------------------------------------------
                 */
                when(agendamentoRepository
                                .saveAndFlush(
                                                any(Agendamento.class)))
                                .thenAnswer(
                                                invocacao -> invocacao.getArgument(0));

                /*
                 * -------------------------------------------------
                 * 8. EXECUTA COM AUTORIZAÇÃO
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 6
                 * perfil = PACIENTE
                 * paciente do usuário = 4
                 * paciente do JSON = 4
                 *
                 * Portanto, a autorização deve ser aceita.
                 */
                AgendamentoResponseDTO resposta = agendamentoService
                                .cadastrarComAutorizacao(
                                                dados,
                                                6L,
                                                TipoPerfil.PACIENTE);

                /*
                 * -------------------------------------------------
                 * 9. VERIFICA
                 * -------------------------------------------------
                 */

                /*
                 * O método deve terminar normalmente
                 * e devolver o DTO.
                 */
                assertNotNull(resposta);

                /*
                 * Confirma que a propriedade do paciente
                 * foi verificada através do usuário autenticado.
                 */
                verify(pacienteRepository)
                                .findByUsuario_IdUsuario(6L);

                /*
                 * Como a autorização e as regras de negócio
                 * foram satisfeitas, o agendamento deve
                 * chegar à persistência.
                 */
                verify(agendamentoRepository)
                                .saveAndFlush(
                                                any(Agendamento.class));
        }

        /*
         * RF07 - autorização por perfil.
         *
         * Um paciente não pode confirmar agendamentos.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         */
        @Test
        void deveBloquearPacienteAoConfirmarAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 *
                 * O registro precisa existir para que a execução
                 * alcance a verificação do perfil.
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA COMO PACIENTE
                 * -------------------------------------------------
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .confirmarComAutorizacao(
                                                                7L,
                                                                6L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O paciente não possui permissão para confirmar agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NÃO HOUVE ALTERAÇÃO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou, o agendamento
                 * não pode chegar ao save().
                 */
                verify(
                                agendamentoRepository,
                                never())
                                .save(any(Agendamento.class));
        }

        /*
         * RF07 - autorização por propriedade.
         *
         * Um profissional não pode confirmar
         * um agendamento atribuído a outro profissional.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void deveBloquearProfissionalAoConfirmarAgendamentoDeOutroProfissional() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Profissional profissionalDoAgendamento = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioDoProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * O agendamento pertence a um profissional
                 * vinculado ao usuário ID 8.
                 */
                when(agendamento.getProfissional())
                                .thenReturn(
                                                profissionalDoAgendamento);

                when(profissionalDoAgendamento.getUsuario())
                                .thenReturn(
                                                usuarioDoProfissional);

                when(usuarioDoProfissional.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * O agendamento de ID 7 existe.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA COMO OUTRO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * Usuário autenticado = 10
                 *
                 * Usuário do profissional do agendamento = 8
                 *
                 * Portanto, são profissionais diferentes.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .confirmarComAutorizacao(
                                                                7L,
                                                                10L,
                                                                TipoPerfil.PROFISSIONAL));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O profissional só pode confirmar os próprios agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou,
                 * o método confirmar(id) não deve chegar
                 * à persistência.
                 */
                verify(
                                agendamentoRepository,
                                never())
                                .save(any(Agendamento.class));
        }

        /*
         * RF07 - cenário positivo de autorização.
         *
         * Um profissional pode confirmar um agendamento
         * quando o atendimento estiver atribuído a ele próprio.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void devePermitirProfissionalConfirmarProprioAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Utilizamos uma entidade real porque queremos
                 * verificar a mudança efetiva do status:
                 *
                 * AGENDADO -> CONFIRMADO
                 */
                Agendamento agendamento = new Agendamento();

                agendamento.setStatus(
                                StatusAgendamento.AGENDADO);

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 *
                 * Esses relacionamentos são necessários porque,
                 * depois da confirmação, converterParaDTO()
                 * acessa os dados do paciente.
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * O profissional do agendamento pertence
                 * ao usuário ID 8.
                 */
                when(usuarioProfissional.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * -------------------------------------------------
                 * 4. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * -------------------------------------------------
                 * 5. RELACIONAMENTOS
                 * -------------------------------------------------
                 */
                agendamento.setPaciente(paciente);
                agendamento.setProfissional(profissional);
                agendamento.setServico(servico);

                /*
                 * -------------------------------------------------
                 * 6. REPOSITORY
                 * -------------------------------------------------
                 *
                 * confirmarComAutorizacao() busca o agendamento
                 * para verificar a propriedade.
                 *
                 * Depois confirmar(id) realiza novamente a busca.
                 *
                 * O mesmo stub atende às duas chamadas.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 7. EXECUTA COMO O PRÓPRIO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 8
                 * usuário do profissional = 8
                 *
                 * Portanto, a confirmação deve ser autorizada.
                 */
                AgendamentoResponseDTO resposta = agendamentoService
                                .confirmarComAutorizacao(
                                                7L,
                                                8L,
                                                TipoPerfil.PROFISSIONAL);

                /*
                 * -------------------------------------------------
                 * 8. CONFERE A AUTORIZAÇÃO E A CONFIRMAÇÃO
                 * -------------------------------------------------
                 */

                /*
                 * A operação deve devolver um DTO normalmente.
                 */
                assertNotNull(resposta);

                /*
                 * RF07:
                 * o status precisa ter sido alterado.
                 */
                assertEquals(
                                StatusAgendamento.CONFIRMADO,
                                agendamento.getStatus());

                /*
                 * -------------------------------------------------
                 * 9. CONFIRMA A PERSISTÊNCIA
                 * -------------------------------------------------
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RF08 - autorização por propriedade.
         *
         * Um paciente autenticado não pode cancelar
         * um agendamento pertencente a outro paciente.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void deveBloquearPacienteAoCancelarAgendamentoDeOutroPaciente() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Paciente pacienteDoAgendamento = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioDoPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * O agendamento pertence a um paciente
                 * vinculado ao usuário ID 6.
                 */
                when(agendamento.getPaciente())
                                .thenReturn(
                                                pacienteDoAgendamento);

                when(pacienteDoAgendamento.getUsuario())
                                .thenReturn(
                                                usuarioDoPaciente);

                when(usuarioDoPaciente.getIdUsuario())
                                .thenReturn(6L);

                /*
                 * O agendamento ID 7 existe.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA COMO OUTRO PACIENTE
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 10
                 * usuário do paciente do agendamento = 6
                 *
                 * Portanto, são pacientes diferentes.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .cancelarComAutorizacao(
                                                                7L,
                                                                10L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O paciente só pode cancelar os próprios agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NÃO HOUVE CANCELAMENTO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou,
                 * cancelar(id) não deve chegar ao save().
                 */
                verify(
                                agendamentoRepository,
                                never())
                                .save(any(Agendamento.class));
        }

        /*
         * RF08 - cenário positivo de autorização.
         *
         * Um paciente autenticado pode cancelar
         * um agendamento pertencente a ele próprio.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void devePermitirPacienteCancelarProprioAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Utilizamos uma entidade real porque queremos
                 * verificar a alteração do status.
                 */
                Agendamento agendamento = new Agendamento();

                agendamento.setStatus(
                                StatusAgendamento.AGENDADO);

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * O paciente do agendamento pertence
                 * ao usuário autenticado ID 6.
                 */
                when(usuarioPaciente.getIdUsuario())
                                .thenReturn(6L);

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * Esses dados são necessários porque
                 * cancelar(id) converte o resultado para DTO.
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * -------------------------------------------------
                 * 4. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * -------------------------------------------------
                 * 5. RELACIONAMENTOS
                 * -------------------------------------------------
                 */
                agendamento.setPaciente(paciente);
                agendamento.setProfissional(profissional);
                agendamento.setServico(servico);

                /*
                 * -------------------------------------------------
                 * 6. REPOSITORY
                 * -------------------------------------------------
                 *
                 * cancelarComAutorizacao() busca uma vez
                 * para validar a propriedade.
                 *
                 * Depois cancelar(id) busca novamente.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 7. EXECUTA COMO O PRÓPRIO PACIENTE
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 6
                 * usuário do paciente = 6
                 *
                 * Portanto, o cancelamento deve ser permitido.
                 */
                AgendamentoResponseDTO resposta = agendamentoService
                                .cancelarComAutorizacao(
                                                7L,
                                                6L,
                                                TipoPerfil.PACIENTE);

                /*
                 * -------------------------------------------------
                 * 8. VERIFICA
                 * -------------------------------------------------
                 */
                assertNotNull(resposta);

                assertEquals(
                                StatusAgendamento.CANCELADO,
                                agendamento.getStatus());

                /*
                 * -------------------------------------------------
                 * 9. CONFIRMA A PERSISTÊNCIA
                 * -------------------------------------------------
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RF08 - autorização por propriedade.
         *
         * Um profissional não pode cancelar
         * um agendamento atribuído a outro profissional.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void deveBloquearProfissionalAoCancelarAgendamentoDeOutroProfissional() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Profissional profissionalDoAgendamento = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioDoProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * O agendamento pertence a um profissional
                 * vinculado ao usuário ID 8.
                 */
                when(agendamento.getProfissional())
                                .thenReturn(
                                                profissionalDoAgendamento);

                when(profissionalDoAgendamento.getUsuario())
                                .thenReturn(
                                                usuarioDoProfissional);

                when(usuarioDoProfissional.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * O agendamento ID 7 existe.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA COMO OUTRO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 10
                 * usuário do profissional do agendamento = 8
                 *
                 * Portanto, são profissionais diferentes.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .cancelarComAutorizacao(
                                                                7L,
                                                                10L,
                                                                TipoPerfil.PROFISSIONAL));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O profissional só pode cancelar os próprios agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 4. GARANTE QUE NÃO HOUVE CANCELAMENTO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou,
                 * cancelar(id) não pode chegar ao save().
                 */
                verify(
                                agendamentoRepository,
                                never()).save(
                                                any(Agendamento.class));
        }

        /*
         * RF08 - cenário positivo de autorização.
         *
         * Um profissional autenticado pode cancelar
         * um agendamento atribuído a ele próprio.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void devePermitirProfissionalCancelarProprioAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. AGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Usamos uma entidade real para verificar
                 * a alteração do status.
                 */
                Agendamento agendamento = new Agendamento();

                agendamento.setStatus(
                                StatusAgendamento.AGENDADO);

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE
                 * -------------------------------------------------
                 *
                 * Necessário para converterParaDTO()
                 * depois do cancelamento.
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 3. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * O profissional do agendamento pertence
                 * ao usuário autenticado ID 8.
                 */
                when(usuarioProfissional.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * -------------------------------------------------
                 * 4. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * -------------------------------------------------
                 * 5. RELACIONAMENTOS
                 * -------------------------------------------------
                 */
                agendamento.setPaciente(paciente);
                agendamento.setProfissional(profissional);
                agendamento.setServico(servico);

                /*
                 * -------------------------------------------------
                 * 6. REPOSITORY
                 * -------------------------------------------------
                 *
                 * cancelarComAutorizacao() consulta o agendamento
                 * para validar o profissional.
                 *
                 * Depois cancelar(id) realiza nova consulta.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 7. EXECUTA COMO O PRÓPRIO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 8
                 * usuário do profissional = 8
                 *
                 * Portanto, o cancelamento deve ser permitido.
                 */
                AgendamentoResponseDTO resposta = agendamentoService
                                .cancelarComAutorizacao(
                                                7L,
                                                8L,
                                                TipoPerfil.PROFISSIONAL);

                /*
                 * -------------------------------------------------
                 * 8. CONFERE O RESULTADO
                 * -------------------------------------------------
                 */
                assertNotNull(resposta);

                assertEquals(
                                StatusAgendamento.CANCELADO,
                                agendamento.getStatus());

                /*
                 * -------------------------------------------------
                 * 9. CONFIRMA A PERSISTÊNCIA
                 * -------------------------------------------------
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RF08 - autorização por propriedade.
         *
         * Um paciente autenticado não pode reagendar
         * um agendamento pertencente a outro paciente.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void deveBloquearPacienteAoReagendarAgendamentoDeOutroPaciente() {

                /*
                 * -------------------------------------------------
                 * 1. NOVOS DADOS DO REAGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Os dados são válidos, mas a autorização
                 * deve bloquear antes da RN04.
                 */
                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(
                                LocalDate.of(2026, 9, 8));

                dados.setHora(
                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 2. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Paciente pacienteDoAgendamento = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioDoPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * O paciente proprietário do agendamento
                 * está vinculado ao usuário ID 6.
                 */
                when(agendamento.getPaciente())
                                .thenReturn(pacienteDoAgendamento);

                when(pacienteDoAgendamento.getUsuario())
                                .thenReturn(usuarioDoPaciente);

                when(usuarioDoPaciente.getIdUsuario())
                                .thenReturn(6L);

                /*
                 * O agendamento ID 7 existe.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 3. EXECUTA COMO OUTRO PACIENTE
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 10
                 * usuário proprietário = 6
                 *
                 * Portanto:
                 * 10 != 6
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .reagendarComAutorizacao(
                                                                7L,
                                                                dados,
                                                                10L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 4. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O paciente só pode reagendar os próprios agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 5. GARANTE QUE NADA FOI ALTERADO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou,
                 * reagendar(id, dados) não deve chegar
                 * à persistência.
                 */
                verify(
                                agendamentoRepository,
                                never()).save(
                                                any(Agendamento.class));
        }

        /*
         * RF08 - cenário positivo de autorização.
         *
         * Um paciente autenticado pode reagendar
         * um agendamento pertencente a ele próprio,
         * desde que o novo horário esteja disponível.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void devePermitirPacienteReagendarProprioAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. NOVA DATA E HORÁRIO
                 * -------------------------------------------------
                 */
                LocalDate novaData = LocalDate.of(2026, 9, 8);

                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(novaData);
                dados.setHora(
                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 2. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                /*
                 * -------------------------------------------------
                 * 3. PACIENTE
                 * -------------------------------------------------
                 *
                 * O paciente do agendamento pertence
                 * ao usuário autenticado ID 6.
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(agendamento.getPaciente())
                                .thenReturn(paciente);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getIdUsuario())
                                .thenReturn(6L);

                /*
                 * Utilizado pelo converterParaDTO().
                 */
                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 4. PROFISSIONAL
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(agendamento.getProfissional())
                                .thenReturn(profissional);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * -------------------------------------------------
                 * 5. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(agendamento.getServico())
                                .thenReturn(servico);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * O serviço possui duração de 60 minutos.
                 */
                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 6. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * O profissional atende das 08:00 às 12:00.
                 *
                 * Portanto:
                 * 10:00 até 11:00 cabe completamente
                 * dentro da disponibilidade.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 7. REPOSITORIES
                 * -------------------------------------------------
                 *
                 * reagendarComAutorizacao() busca o agendamento
                 * para verificar a propriedade.
                 *
                 * Depois reagendar() realiza nova busca.
                 *
                 * O mesmo mock atende às duas chamadas.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * Disponibilidade do profissional
                 * na nova data.
                 */
                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * Nenhum outro agendamento ocupa
                 * o novo horário.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                novaData,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of());

                /*
                 * Simula a persistência da alteração.
                 */
                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 8. EXECUTA COMO O PRÓPRIO PACIENTE
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 6
                 * usuário do paciente = 6
                 *
                 * Portanto, a autorização deve ser aceita.
                 */
                AgendamentoResponseDTO resposta = agendamentoService
                                .reagendarComAutorizacao(
                                                7L,
                                                dados,
                                                6L,
                                                TipoPerfil.PACIENTE);

                /*
                 * -------------------------------------------------
                 * 9. CONFERE O RESULTADO
                 * -------------------------------------------------
                 */
                assertNotNull(resposta);

                /*
                 * Confirma que a nova data foi aplicada.
                 */
                verify(agendamento)
                                .setData(novaData);

                /*
                 * Confirma que o novo horário foi aplicado.
                 */
                verify(agendamento)
                                .setHora(
                                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 10. CONFIRMA A PERSISTÊNCIA
                 * -------------------------------------------------
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RF08 - autorização por propriedade.
         *
         * Um profissional não pode reagendar
         * um atendimento atribuído a outro profissional.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void deveBloquearProfissionalAoReagendarAgendamentoDeOutroProfissional() {

                /*
                 * -------------------------------------------------
                 * 1. NOVOS DADOS DO REAGENDAMENTO
                 * -------------------------------------------------
                 *
                 * Os dados podem ser válidos, pois esperamos
                 * que a autorização interrompa a operação antes
                 * das regras de disponibilidade e conflito.
                 */
                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(
                                LocalDate.of(2026, 9, 8));

                dados.setHora(
                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 2. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                Profissional profissionalDoAgendamento = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioDoProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * O atendimento está atribuído a um profissional
                 * vinculado ao usuário ID 8.
                 */
                when(agendamento.getProfissional())
                                .thenReturn(
                                                profissionalDoAgendamento);

                when(profissionalDoAgendamento.getUsuario())
                                .thenReturn(
                                                usuarioDoProfissional);

                when(usuarioDoProfissional.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * O agendamento ID 7 existe.
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * -------------------------------------------------
                 * 3. EXECUTA COMO OUTRO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 10
                 * usuário do profissional do agendamento = 8
                 *
                 * Portanto:
                 * 10 != 8
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService
                                                .reagendarComAutorizacao(
                                                                7L,
                                                                dados,
                                                                10L,
                                                                TipoPerfil.PROFISSIONAL));

                /*
                 * -------------------------------------------------
                 * 4. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O profissional só pode reagendar os próprios agendamentos.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 5. GARANTE QUE NADA FOI ALTERADO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou,
                 * reagendar(id, dados) não deve chegar ao save().
                 */
                verify(
                                agendamentoRepository,
                                never()).save(
                                                any(Agendamento.class));
        }

        /*
         * RF08 - cenário positivo de autorização.
         *
         * Um profissional autenticado pode reagendar
         * um atendimento atribuído a ele próprio,
         * desde que o novo horário esteja disponível.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         */
        @Test
        void devePermitirProfissionalReagendarProprioAgendamento() {

                /*
                 * -------------------------------------------------
                 * 1. NOVA DATA E HORÁRIO
                 * -------------------------------------------------
                 */
                LocalDate novaData = LocalDate.of(2026, 9, 8);

                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();

                dados.setData(novaData);

                dados.setHora(
                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 2. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 */
                Agendamento agendamento = org.mockito.Mockito.mock(
                                Agendamento.class);

                /*
                 * -------------------------------------------------
                 * 3. PACIENTE
                 * -------------------------------------------------
                 *
                 * Necessário para montar o DTO de resposta.
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(agendamento.getPaciente())
                                .thenReturn(paciente);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 4. PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * O atendimento pertence ao profissional
                 * vinculado ao usuário ID 8.
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(agendamento.getProfissional())
                                .thenReturn(profissional);

                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissional.getUsuario())
                                .thenReturn(usuarioProfissional);

                when(usuarioProfissional.getIdUsuario())
                                .thenReturn(8L);

                when(usuarioProfissional.getNome())
                                .thenReturn("Profissional Teste");

                /*
                 * -------------------------------------------------
                 * 5. SERVIÇO
                 * -------------------------------------------------
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(agendamento.getServico())
                                .thenReturn(servico);

                when(servico.getIdServico())
                                .thenReturn(1L);

                when(servico.getNome())
                                .thenReturn("Fisioterapia");

                /*
                 * Serviço com duração de 60 minutos.
                 */
                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 6. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * Profissional disponível:
                 * 08:00 até 12:00.
                 *
                 * Novo atendimento:
                 * 10:00 até 11:00.
                 */
                Disponibilidade disponibilidade = org.mockito.Mockito.mock(
                                Disponibilidade.class);

                when(disponibilidade.getHoraInicio())
                                .thenReturn(
                                                LocalTime.of(8, 0));

                when(disponibilidade.getHoraFim())
                                .thenReturn(
                                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 7. REPOSITORIES
                 * -------------------------------------------------
                 *
                 * A primeira busca é utilizada pela autorização.
                 *
                 * A segunda ocorre dentro de reagendar().
                 */
                when(agendamentoRepository.findById(7L))
                                .thenReturn(
                                                Optional.of(agendamento));

                /*
                 * 08/09/2026 é terça-feira.
                 *
                 * Retornamos uma disponibilidade válida.
                 */
                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * Nenhum outro agendamento ocupa
                 * o novo horário.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                novaData,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of());

                /*
                 * Simula a persistência da alteração.
                 */
                when(agendamentoRepository.save(agendamento))
                                .thenReturn(agendamento);

                /*
                 * -------------------------------------------------
                 * 8. EXECUTA COMO O PRÓPRIO PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 8
                 * usuário do profissional = 8
                 *
                 * Portanto, a autorização deve ser aceita.
                 */
                AgendamentoResponseDTO resposta = agendamentoService
                                .reagendarComAutorizacao(
                                                7L,
                                                dados,
                                                8L,
                                                TipoPerfil.PROFISSIONAL);

                /*
                 * -------------------------------------------------
                 * 9. CONFERE O RESULTADO
                 * -------------------------------------------------
                 */
                assertNotNull(resposta);

                /*
                 * A nova data deve ser aplicada.
                 */
                verify(agendamento)
                                .setData(novaData);

                /*
                 * O novo horário deve ser aplicado.
                 */
                verify(agendamento)
                                .setHora(
                                                LocalTime.of(10, 0));

                /*
                 * -------------------------------------------------
                 * 10. CONFIRMA A PERSISTÊNCIA
                 * -------------------------------------------------
                 */
                verify(agendamentoRepository)
                                .save(agendamento);
        }

        /*
         * RF08 - Cancelamento de agendamento.
         *
         * Um agendamento que já está cancelado
         * não pode ser cancelado novamente.
         *
         * Tópicos:
         * - Testes de Software
         * - Engenharia de Software
         * - Programação Orientada a Objetos
         */
        @Test
        void deveBloquearCancelamentoDeAgendamentoJaCancelado() {

                // 1. Simula um agendamento já cancelado.
                Agendamento agendamento = new Agendamento();
                agendamento.setStatus(StatusAgendamento.CANCELADO);

                // 2. Simula a consulta ao banco de dados.
                when(agendamentoRepository.findById(7L))
                                .thenReturn(Optional.of(agendamento));

                // 3. Verifica se a Service impede o cancelamento.
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService.cancelar(7L));

                // 4. Confirma que a mensagem de erro está correta.
                assertEquals(
                                "Este agendamento já está cancelado.",
                                excecao.getMessage());

                // 5. Garante que nenhum dado seja salvo.
                verify(agendamentoRepository, never()).save(any(Agendamento.class));
        }

        /*
         * RF08 - Reagendamento de agendamento.
         *
         * Um atendimento cancelado não pode
         * receber uma nova data ou horário.
         *
         * Tópicos:
         * - Testes de Software
         * - Engenharia de Software
         * - Programação Orientada a Objetos
         */
        @Test
        void deveBloquearReagendamentoDeAgendamentoCancelado() {

                // 1. Simula um agendamento cancelado.
                Agendamento agendamento = new Agendamento();
                agendamento.setStatus(StatusAgendamento.CANCELADO);

                // 2. Prepara os dados de uma tentativa de reagendamento.
                ReagendamentoRequestDTO dados = new ReagendamentoRequestDTO();
                dados.setData(LocalDate.of(2026, 10, 15));
                dados.setHora(LocalTime.of(9, 0));

                // 3. Simula a busca do agendamento no Repository.
                when(agendamentoRepository.findById(7L))
                                .thenReturn(Optional.of(agendamento));

                // 4. Verifica se a Service bloqueia o reagendamento.
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> agendamentoService.reagendar(7L, dados));

                // 5. Confirma a mensagem de erro.
                assertEquals(
                                "Não é possível reagendar um atendimento cancelado.",
                                excecao.getMessage());

                // 6. Garante que o agendamento não seja salvo.
                verify(agendamentoRepository, never()).save(any(Agendamento.class));
        }

}
