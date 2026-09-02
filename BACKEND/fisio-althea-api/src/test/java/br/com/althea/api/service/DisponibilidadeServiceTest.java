package br.com.althea.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;

import br.com.althea.api.dto.DisponibilidadeRequestDTO;
import br.com.althea.api.dto.DisponibilidadeResponseDTO;
import br.com.althea.api.dto.HorariosDisponiveisResponseDTO;

import br.com.althea.api.entity.Agendamento;
import br.com.althea.api.entity.DiaSemana;
import br.com.althea.api.entity.Disponibilidade;
import br.com.althea.api.entity.Profissional;
import br.com.althea.api.entity.Servico;
import br.com.althea.api.entity.StatusAgendamento;
import br.com.althea.api.entity.TipoPerfil;
import br.com.althea.api.entity.Usuario;

import br.com.althea.api.repository.AgendamentoRepository;
import br.com.althea.api.repository.DisponibilidadeRepository;
import br.com.althea.api.repository.ProfissionalRepository;
import br.com.althea.api.repository.ServicoRepository;

/*
 * Testes da consulta de horários disponíveis.
 *
 * Aqui utilizamos Mockito para testar somente
 * as regras da DisponibilidadeService,
 * sem alterar registros reais do MySQL.
 *
 * Tópicos:
 * - Testes de Software
 * - Levantamento e Especificação de Requisitos
 * - Engenharia de Software
 * - Spring Data JPA
 * - Arquitetura de Software
 */
@ExtendWith(MockitoExtension.class)
class DisponibilidadeServiceTest {

        @Mock
        private ProfissionalRepository profissionalRepository;

        @Mock
        private DisponibilidadeRepository disponibilidadeRepository;

        @Mock
        private ServicoRepository servicoRepository;

        @Mock
        private AgendamentoRepository agendamentoRepository;

        /*
         * O Mockito cria a DisponibilidadeService
         * usando os mocks declarados acima.
         */
        @InjectMocks
        private DisponibilidadeService disponibilidadeService;

        /*
         * RF05
         *
         * Deve retornar somente horários que:
         *
         * - estejam dentro da disponibilidade;
         * - comportem toda a duração do serviço;
         * - não estejam ocupados por outro agendamento.
         */
        @Test
        void deveRetornarSomenteHorariosDisponiveis() {

                /*
                 * -------------------------------------------------
                 * 1. DATA DA CONSULTA
                 * -------------------------------------------------
                 *
                 * 01/09/2026 corresponde a uma terça-feira.
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                /*
                 * -------------------------------------------------
                 * 3. SERVIÇO
                 * -------------------------------------------------
                 *
                 * O serviço possui duração de 60 minutos.
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(servico));

                /*
                 * -------------------------------------------------
                 * 4. DISPONIBILIDADE
                 * -------------------------------------------------
                 *
                 * Profissional:
                 *
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

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * -------------------------------------------------
                 * 5. AGENDAMENTO DAS 09:00
                 * -------------------------------------------------
                 */
                Agendamento agendamento09 = org.mockito.Mockito.mock(
                                Agendamento.class);

                Servico servico09 = org.mockito.Mockito.mock(
                                Servico.class);

                when(agendamento09.getHora())
                                .thenReturn(
                                                LocalTime.of(9, 0));

                when(agendamento09.getServico())
                                .thenReturn(servico09);

                when(servico09.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * -------------------------------------------------
                 * 6. AGENDAMENTO DAS 10:00
                 * -------------------------------------------------
                 */
                Agendamento agendamento10 = org.mockito.Mockito.mock(
                                Agendamento.class);

                Servico servico10 = org.mockito.Mockito.mock(
                                Servico.class);

                when(agendamento10.getHora())
                                .thenReturn(
                                                LocalTime.of(10, 0));

                when(agendamento10.getServico())
                                .thenReturn(servico10);

                when(servico10.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * A consulta retorna somente agendamentos
                 * que ainda ocupam a agenda.
                 *
                 * CANCELADO é ignorado.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of(
                                                                agendamento09,
                                                                agendamento10));

                /*
                 * -------------------------------------------------
                 * 7. EXECUTA A RF05
                 * -------------------------------------------------
                 */
                HorariosDisponiveisResponseDTO resposta = disponibilidadeService
                                .buscarHorariosDisponiveis(
                                                3L,
                                                1L,
                                                data);

                /*
                 * -------------------------------------------------
                 * 8. VERIFICA O RESULTADO
                 * -------------------------------------------------
                 *
                 * 09:00 e 10:00 estão ocupados.
                 *
                 * Portanto devem restar:
                 *
                 * 08:00
                 * 11:00
                 */
                assertEquals(
                                List.of(
                                                LocalTime.of(8, 0),
                                                LocalTime.of(11, 0)),
                                resposta.getHorarios());
        }

        /*
         * RF05 + RN01
         *
         * Deve remover horários que possuam
         * sobreposição parcial com um atendimento
         * já existente.
         *
         * Exemplo:
         *
         * agendamento existente:
         * 09:30 até 10:30
         *
         * horários de 60 minutos:
         *
         * 08:00 até 09:00 -> livre
         * 09:00 até 10:00 -> conflito
         * 10:00 até 11:00 -> conflito
         * 11:00 até 12:00 -> livre
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Engenharia de Software
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @Test
        void deveRemoverHorariosComSobreposicaoParcial() {

                /*
                 * -------------------------------------------------
                 * 1. DATA
                 * -------------------------------------------------
                 *
                 * 01/09/2026 = terça-feira.
                 */
                LocalDate data = LocalDate.of(
                                2026,
                                9,
                                1);

                /*
                 * -------------------------------------------------
                 * 2. SERVIÇO CONSULTADO
                 * -------------------------------------------------
                 *
                 * Duração:
                 * 60 minutos.
                 */
                Servico servico = org.mockito.Mockito.mock(
                                Servico.class);

                when(servico.getDuracaoMinutos())
                                .thenReturn(60);

                when(servicoRepository.findById(1L))
                                .thenReturn(
                                                Optional.of(servico));

                /*
                 * -------------------------------------------------
                 * 3. DISPONIBILIDADE
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

                when(disponibilidadeRepository
                                .findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
                                                3L,
                                                DiaSemana.TERCA))
                                .thenReturn(
                                                List.of(disponibilidade));

                /*
                 * -------------------------------------------------
                 * 4. AGENDAMENTO EXISTENTE
                 * -------------------------------------------------
                 *
                 * Atendimento já marcado:
                 *
                 * 09:30 até 10:30.
                 */
                Agendamento existente = org.mockito.Mockito.mock(
                                Agendamento.class);

                Servico servicoExistente = org.mockito.Mockito.mock(
                                Servico.class);

                when(existente.getHora())
                                .thenReturn(
                                                LocalTime.of(9, 30));

                when(existente.getServico())
                                .thenReturn(
                                                servicoExistente);

                when(servicoExistente.getDuracaoMinutos())
                                .thenReturn(60);

                /*
                 * O Repository devolve o agendamento
                 * que atualmente ocupa parte da agenda.
                 */
                when(agendamentoRepository
                                .findByProfissional_IdProfissionalAndDataAndStatusNot(
                                                3L,
                                                data,
                                                StatusAgendamento.CANCELADO))
                                .thenReturn(
                                                List.of(existente));

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA
                 * -------------------------------------------------
                 */
                HorariosDisponiveisResponseDTO resposta = disponibilidadeService
                                .buscarHorariosDisponiveis(
                                                3L,
                                                1L,
                                                data);

                /*
                 * -------------------------------------------------
                 * 6. VERIFICA
                 * -------------------------------------------------
                 *
                 * 09:00 e 10:00 possuem sobreposição
                 * com o atendimento das 09:30 às 10:30.
                 *
                 * Portanto devem restar:
                 *
                 * 08:00
                 * 11:00
                 */
                assertEquals(
                                List.of(
                                                LocalTime.of(8, 0),
                                                LocalTime.of(11, 0)),
                                resposta.getHorarios());
        }

        /*
         * RF04 - cenário negativo.
         *
         * Não deve permitir cadastrar uma disponibilidade
         * para um profissional que não existe no sistema.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveBloquearCadastroDeDisponibilidadeParaProfissionalInexistente() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DA DISPONIBILIDADE
                 * -------------------------------------------------
                 */
                DisponibilidadeRequestDTO dados = new DisponibilidadeRequestDTO();

                dados.setIdProfissional(999L);

                dados.setDiaSemana(
                                DiaSemana.TERCA);

                dados.setHoraInicio(
                                LocalTime.of(8, 0));

                dados.setHoraFim(
                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 2. PROFISSIONAL INEXISTENTE
                 * -------------------------------------------------
                 *
                 * Optional.empty() representa a situação
                 * em que o ID informado não existe.
                 */
                when(profissionalRepository.findById(999L))
                                .thenReturn(
                                                Optional.empty());

                /*
                 * -------------------------------------------------
                 * 3. EXECUTA
                 * -------------------------------------------------
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> disponibilidadeService
                                                .cadastrar(dados));

                /*
                 * -------------------------------------------------
                 * 4. CONFIRMA A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "Profissional não encontrado.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 5. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Como o profissional não existe,
                 * nenhuma Disponibilidade deve ser
                 * enviada ao Repository.
                 */
                verify(
                                disponibilidadeRepository,
                                never()).save(
                                                org.mockito.ArgumentMatchers
                                                                .any(Disponibilidade.class));
        }

        /*
         * RF04 - cenário positivo.
         *
         * Deve cadastrar uma disponibilidade válida
         * para um profissional existente.
         *
         * Também verificamos:
         *
         * - vínculo com o profissional;
         * - dia da semana;
         * - horário inicial;
         * - horário final;
         * - ativo = true.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Banco de Dados Relacional
         */
        @Test
        void deveCadastrarDisponibilidadeParaProfissionalExistente() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS RECEBIDOS PELA API
                 * -------------------------------------------------
                 */
                DisponibilidadeRequestDTO dados = new DisponibilidadeRequestDTO();

                dados.setIdProfissional(3L);

                dados.setDiaSemana(
                                DiaSemana.TERCA);

                dados.setHoraInicio(
                                LocalTime.of(8, 0));

                dados.setHoraFim(
                                LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 2. PROFISSIONAL EXISTENTE
                 * -------------------------------------------------
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                /*
                 * O converterParaDTO utiliza o ID
                 * do profissional relacionado.
                 */
                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                /*
                 * -------------------------------------------------
                 * 3. SIMULA O SALVAMENTO
                 * -------------------------------------------------
                 *
                 * A Service cria uma Disponibilidade real.
                 *
                 * O Repository simulado devolve exatamente
                 * o objeto recebido.
                 */
                when(disponibilidadeRepository
                                .save(
                                                org.mockito.ArgumentMatchers
                                                                .any(Disponibilidade.class)))
                                .thenAnswer(
                                                invocacao -> invocacao.getArgument(0));

                /*
                 * -------------------------------------------------
                 * 4. EXECUTA O CADASTRO
                 * -------------------------------------------------
                 */
                DisponibilidadeResponseDTO resposta = disponibilidadeService
                                .cadastrar(dados);

                /*
                 * A operação deve terminar normalmente
                 * e devolver um DTO.
                 */
                assertNotNull(resposta);

                /*
                 * -------------------------------------------------
                 * 5. CAPTURA A DISPONIBILIDADE SALVA
                 * -------------------------------------------------
                 */
                ArgumentCaptor<Disponibilidade> captor = ArgumentCaptor.forClass(
                                Disponibilidade.class);

                verify(disponibilidadeRepository)
                                .save(
                                                captor.capture());

                Disponibilidade disponibilidadeSalva = captor.getValue();

                /*
                 * -------------------------------------------------
                 * 6. CONFERE O PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * A disponibilidade deve estar vinculada
                 * exatamente ao profissional encontrado.
                 */
                assertSame(
                                profissional,
                                disponibilidadeSalva.getProfissional());

                /*
                 * -------------------------------------------------
                 * 7. CONFERE O DIA DA SEMANA
                 * -------------------------------------------------
                 */
                assertEquals(
                                DiaSemana.TERCA,
                                disponibilidadeSalva.getDiaSemana());

                /*
                 * -------------------------------------------------
                 * 8. CONFERE OS HORÁRIOS
                 * -------------------------------------------------
                 */
                assertEquals(
                                LocalTime.of(8, 0),
                                disponibilidadeSalva.getHoraInicio());

                assertEquals(
                                LocalTime.of(12, 0),
                                disponibilidadeSalva.getHoraFim());

                /*
                 * -------------------------------------------------
                 * 9. CONFERE O STATUS
                 * -------------------------------------------------
                 *
                 * Toda nova disponibilidade deve começar ativa.
                 */
                assertEquals(
                                true,
                                disponibilidadeSalva.getAtivo());
        }

        /*
         * RF04 - autorização por propriedade.
         *
         * Um usuário autenticado com perfil PROFISSIONAL
         * não pode cadastrar disponibilidade para
         * outro profissional.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Arquitetura de Software
         */
        @Test
        void deveBloquearProfissionalAoCadastrarDisponibilidadeParaOutroProfissional() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS RECEBIDOS PELA API
                 * -------------------------------------------------
                 *
                 * O profissional informado no JSON será o ID 3.
                 */
                DisponibilidadeRequestDTO dados = new DisponibilidadeRequestDTO();

                dados.setIdProfissional(3L);
                dados.setDiaSemana(DiaSemana.TERCA);
                dados.setHoraInicio(LocalTime.of(8, 0));
                dados.setHoraFim(LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 2. PROFISSIONAL INFORMADO NO JSON
                 * -------------------------------------------------
                 *
                 * Esse profissional está relacionado
                 * ao usuário de ID 8.
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuarioDoProfissional = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getUsuario())
                                .thenReturn(usuarioDoProfissional);

                when(usuarioDoProfissional.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * O Repository encontra normalmente
                 * o profissional informado.
                 */
                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                /*
                 * -------------------------------------------------
                 * 3. USUÁRIO AUTENTICADO
                 * -------------------------------------------------
                 *
                 * Imagine que o JWT pertença ao usuário 10.
                 *
                 * Portanto:
                 *
                 * usuário autenticado = 10
                 * usuário do profissional informado = 8
                 *
                 * São pessoas diferentes.
                 */
                Long idUsuarioAutenticado = 10L;

                /*
                 * -------------------------------------------------
                 * 4. EXECUTA A OPERAÇÃO
                 * -------------------------------------------------
                 *
                 * O perfil autenticado é PROFISSIONAL.
                 *
                 * Como ele está tentando cadastrar
                 * disponibilidade para outro profissional,
                 * esperamos o bloqueio.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> disponibilidadeService
                                                .cadastrarComAutorizacao(
                                                                dados,
                                                                idUsuarioAutenticado,
                                                                TipoPerfil.PROFISSIONAL));

                /*
                 * -------------------------------------------------
                 * 5. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O profissional só pode cadastrar a própria disponibilidade.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 6. GARANTE QUE NADA FOI SALVO
                 * -------------------------------------------------
                 *
                 * Como a autorização falhou,
                 * nenhuma disponibilidade pode chegar
                 * ao Repository.
                 */
                verify(
                                disponibilidadeRepository,
                                never()).save(
                                                org.mockito.ArgumentMatchers
                                                                .any(Disponibilidade.class));
        }

        /*
         * RF04 - autorização por propriedade.
         *
         * Um usuário autenticado com perfil PROFISSIONAL
         * pode cadastrar disponibilidade para ele próprio.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         * - Arquitetura de Software
         */
        @Test
        void devePermitirProfissionalCadastrarPropriaDisponibilidade() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DA DISPONIBILIDADE
                 * -------------------------------------------------
                 */
                DisponibilidadeRequestDTO dados = new DisponibilidadeRequestDTO();

                dados.setIdProfissional(3L);
                dados.setDiaSemana(DiaSemana.TERCA);
                dados.setHoraInicio(LocalTime.of(8, 0));
                dados.setHoraFim(LocalTime.of(12, 0));

                /*
                 * -------------------------------------------------
                 * 2. PROFISSIONAL E USUÁRIO RELACIONADOS
                 * -------------------------------------------------
                 *
                 * O profissional ID 3 pertence ao usuário ID 8.
                 */
                Profissional profissional = org.mockito.Mockito.mock(
                                Profissional.class);

                Usuario usuario = org.mockito.Mockito.mock(
                                Usuario.class);

                when(profissional.getUsuario())
                                .thenReturn(usuario);

                when(usuario.getIdUsuario())
                                .thenReturn(8L);

                /*
                 * O converterParaDTO utiliza também
                 * o ID do profissional.
                 */
                when(profissional.getIdProfissional())
                                .thenReturn(3L);

                /*
                 * -------------------------------------------------
                 * 3. SIMULA A BUSCA DO PROFISSIONAL
                 * -------------------------------------------------
                 */
                when(profissionalRepository.findById(3L))
                                .thenReturn(
                                                Optional.of(profissional));

                /*
                 * -------------------------------------------------
                 * 4. SIMULA O SALVAMENTO
                 * -------------------------------------------------
                 */
                when(disponibilidadeRepository.save(
                                org.mockito.ArgumentMatchers
                                                .any(Disponibilidade.class)))
                                .thenAnswer(
                                                invocacao -> invocacao.getArgument(0));

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA
                 * -------------------------------------------------
                 *
                 * O usuário autenticado também é o ID 8.
                 *
                 * Portanto:
                 *
                 * usuário autenticado = 8
                 * usuário do profissional = 8
                 */
                DisponibilidadeResponseDTO resposta = disponibilidadeService
                                .cadastrarComAutorizacao(
                                                dados,
                                                8L,
                                                TipoPerfil.PROFISSIONAL);

                /*
                 * -------------------------------------------------
                 * 6. CONFIRMA QUE A OPERAÇÃO FOI PERMITIDA
                 * -------------------------------------------------
                 */
                assertNotNull(resposta);

                /*
                 * -------------------------------------------------
                 * 7. CAPTURA O OBJETO SALVO
                 * -------------------------------------------------
                 */
                ArgumentCaptor<Disponibilidade> captor = ArgumentCaptor.forClass(
                                Disponibilidade.class);

                verify(disponibilidadeRepository)
                                .save(captor.capture());

                Disponibilidade disponibilidadeSalva = captor.getValue();

                /*
                 * -------------------------------------------------
                 * 8. CONFERE OS DADOS
                 * -------------------------------------------------
                 */
                assertSame(
                                profissional,
                                disponibilidadeSalva.getProfissional());

                assertEquals(
                                DiaSemana.TERCA,
                                disponibilidadeSalva.getDiaSemana());

                assertEquals(
                                LocalTime.of(8, 0),
                                disponibilidadeSalva.getHoraInicio());

                assertEquals(
                                LocalTime.of(12, 0),
                                disponibilidadeSalva.getHoraFim());

                assertEquals(
                                true,
                                disponibilidadeSalva.getAtivo());
        }
}