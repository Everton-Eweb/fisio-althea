package br.com.althea.api.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.althea.api.dto.MensagemResponseDTO;
import br.com.althea.api.dto.MensagemRequestDTO;
import br.com.althea.api.entity.Paciente;
import br.com.althea.api.entity.TipoPerfil;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.entity.Mensagem;

import br.com.althea.api.repository.MensagemRepository;
import br.com.althea.api.repository.PacienteRepository;
import br.com.althea.api.repository.UsuarioRepository;

/*
 * Testes unitários das regras de mensagens.
 *
 * Os repositories são simulados com Mockito,
 * portanto nenhum dado real é alterado no MySQL.
 *
 * Tópicos:
 * - Testes de Software
 * - Segurança
 * - Engenharia de Software
 * - Arquitetura de Software
 */
@ExtendWith(MockitoExtension.class)
class MensagemServiceTest {

        @Mock
        private MensagemRepository mensagemRepository;

        @Mock
        private PacienteRepository pacienteRepository;

        @Mock
        private UsuarioRepository usuarioRepository;

        @Mock
        private EntityManager entityManager;

        @InjectMocks
        private MensagemService mensagemService;

        /*
         * Regra de autorização das mensagens.
         *
         * Um usuário autenticado com perfil PACIENTE
         * somente pode cadastrar mensagens relacionadas
         * ao próprio cadastro de paciente.
         *
         * Tópicos:
         * - Testes de Software
         * - Segurança
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveBloquearPacienteAoEnviarMensagemParaOutroPaciente() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DA MENSAGEM
                 * -------------------------------------------------
                 *
                 * O usuário autenticado será o usuário 6.
                 *
                 * Porém o JSON tentará relacionar a mensagem
                 * ao paciente 99.
                 */
                MensagemRequestDTO dados = new MensagemRequestDTO();

                dados.setIdPaciente(99L);
                dados.setAssunto("Teste");

                dados.setConteudo(
                                "Mensagem de teste automatizado");

                /*
                 * -------------------------------------------------
                 * 2. USUÁRIO AUTENTICADO
                 * -------------------------------------------------
                 *
                 * A MensagemService procura o usuário
                 * autenticado utilizando o ID vindo do JWT.
                 */
                Usuario usuarioAutenticado = org.mockito.Mockito.mock(
                                Usuario.class);

                when(usuarioRepository.findById(6L))
                                .thenReturn(
                                                Optional.of(usuarioAutenticado));

                /*
                 * -------------------------------------------------
                 * 3. PACIENTE INFORMADO NO JSON
                 * -------------------------------------------------
                 *
                 * O paciente 99 existe.
                 *
                 * Portanto o teste deve avançar além
                 * da validação "Paciente não encontrado".
                 */
                Paciente pacienteInformado = org.mockito.Mockito.mock(
                                Paciente.class);

                when(pacienteRepository.findById(99L))
                                .thenReturn(
                                                Optional.of(pacienteInformado));

                /*
                 * -------------------------------------------------
                 * 4. PACIENTE DO USUÁRIO AUTENTICADO
                 * -------------------------------------------------
                 *
                 * O usuário 6 está relacionado ao
                 * paciente de ID 4.
                 */
                Paciente pacienteAutenticado = org.mockito.Mockito.mock(
                                Paciente.class);

                when(pacienteRepository
                                .findByUsuario_IdUsuario(6L))
                                .thenReturn(
                                                Optional.of(pacienteAutenticado));

                when(pacienteAutenticado.getIdPaciente())
                                .thenReturn(4L);

                /*
                 * Temos então:
                 *
                 * paciente autenticado = 4
                 * paciente informado = 99
                 *
                 * Como são diferentes, a operação
                 * deve ser bloqueada.
                 */

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA
                 * -------------------------------------------------
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> mensagemService
                                                .cadastrarComAutorizacao(
                                                                dados,
                                                                6L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 6. CONFIRMA A REGRA
                 * -------------------------------------------------
                 *
                 * Agora também verificamos que a exceção
                 * ocorreu exatamente pela regra de
                 * propriedade do paciente.
                 */
                assertEquals(
                                "O paciente só pode enviar mensagens relacionadas ao próprio cadastro.",
                                excecao.getMessage());

                /*
                 * -------------------------------------------------
                 * 7. CONFIRMA QUE NADA FOI SALVO
                 * -------------------------------------------------
                 */
                verify(
                                mensagemRepository,
                                never()).saveAndFlush(
                                                org.mockito.ArgumentMatchers.any());
        }

        /*
         * Cenário positivo da autorização de mensagens.
         *
         * Um usuário autenticado com perfil PACIENTE
         * pode cadastrar uma mensagem quando o
         * idPaciente informado pertence ao próprio usuário.
         *
         * Além disso, o remetente deve ser obtido
         * do usuário autenticado, e não do JSON.
         *
         * Tópicos:
         * - Testes de Software
         * - Segurança
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void devePermitirPacienteEnviarMensagemDoProprioCadastro() {

                /*
                 * -------------------------------------------------
                 * 1. DADOS DA MENSAGEM
                 * -------------------------------------------------
                 *
                 * O paciente informado é o paciente 4.
                 */
                MensagemRequestDTO dados = new MensagemRequestDTO();

                dados.setIdPaciente(4L);
                dados.setAssunto(
                                "Dúvida sobre atendimento");

                dados.setConteudo(
                                "Mensagem criada durante o teste automatizado.");

                /*
                 * -------------------------------------------------
                 * 2. USUÁRIO AUTENTICADO
                 * -------------------------------------------------
                 *
                 * Esse usuário representa a identidade
                 * obtida através do JWT.
                 */
                Usuario usuarioAutenticado = org.mockito.Mockito.mock(
                                Usuario.class);

                when(usuarioAutenticado.getIdUsuario())
                                .thenReturn(6L);

                when(usuarioAutenticado.getNome())
                                .thenReturn(
                                                "Paciente Teste");

                when(usuarioRepository.findById(6L))
                                .thenReturn(
                                                Optional.of(usuarioAutenticado));

                /*
                 * -------------------------------------------------
                 * 3. PACIENTE
                 * -------------------------------------------------
                 *
                 * O usuário autenticado 6 está relacionado
                 * ao paciente 4.
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                /*
                 * O converterParaDTO utiliza o usuário
                 * relacionado ao paciente para obter o nome.
                 */
                when(paciente.getUsuario())
                                .thenReturn(
                                                usuarioAutenticado);

                /*
                 * Busca pelo paciente informado no JSON.
                 */
                when(pacienteRepository.findById(4L))
                                .thenReturn(
                                                Optional.of(paciente));

                /*
                 * Busca o paciente pertencente ao usuário
                 * autenticado.
                 *
                 * Como também é o paciente 4,
                 * a autorização deve ser aceita.
                 */
                when(pacienteRepository
                                .findByUsuario_IdUsuario(6L))
                                .thenReturn(
                                                Optional.of(paciente));

                /*
                 * -------------------------------------------------
                 * 4. SIMULA O SALVAMENTO
                 * -------------------------------------------------
                 *
                 * A Service cria uma Mensagem real.
                 *
                 * Portanto devolvemos exatamente o objeto
                 * recebido pelo saveAndFlush().
                 */
                when(mensagemRepository
                                .saveAndFlush(
                                                org.mockito.ArgumentMatchers
                                                                .any(Mensagem.class)))
                                .thenAnswer(
                                                invocacao -> invocacao.getArgument(0));

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA
                 * -------------------------------------------------
                 */
                MensagemResponseDTO resposta = mensagemService
                                .cadastrarComAutorizacao(
                                                dados,
                                                6L,
                                                TipoPerfil.PACIENTE);

                /*
                 * -------------------------------------------------
                 * 6. VERIFICA A RESPOSTA
                 * -------------------------------------------------
                 *
                 * Se a autorização estiver correta,
                 * a Service deve concluir a operação
                 * e devolver um DTO.
                 */
                assertNotNull(resposta);

                /*
                 * -------------------------------------------------
                 * 7. CAPTURA A MENSAGEM SALVA
                 * -------------------------------------------------
                 *
                 * Aqui verificamos o objeto real que
                 * foi enviado ao Repository.
                 */
                ArgumentCaptor<Mensagem> captor = ArgumentCaptor.forClass(
                                Mensagem.class);

                verify(mensagemRepository)
                                .saveAndFlush(
                                                captor.capture());

                Mensagem mensagemSalva = captor.getValue();

                /*
                 * O paciente salvo deve ser exatamente
                 * aquele informado e autorizado.
                 */
                assertSame(
                                paciente,
                                mensagemSalva.getPaciente());

                /*
                 * O usuário da mensagem deve ser o usuário
                 * autenticado obtido pelo JWT.
                 */
                assertSame(
                                usuarioAutenticado,
                                mensagemSalva.getUsuario());

                /*
                 * Confirma os dados enviados.
                 */
                assertEquals(
                                "Dúvida sobre atendimento",
                                mensagemSalva.getAssunto());

                assertEquals(
                                "Mensagem criada durante o teste automatizado.",
                                mensagemSalva.getConteudo());

                /*
                 * Toda mensagem nova deve iniciar
                 * com lida = false.
                 */
                assertFalse(
                                mensagemSalva.getLida());

                /*
                 * O refresh também deve ocorrer,
                 * pois a Service o utiliza para recuperar
                 * valores preenchidos pelo banco,
                 * como data_envio.
                 */
                verify(entityManager)
                                .refresh(
                                                mensagemSalva);
        }

        /*
         * Regra de autorização das mensagens.
         *
         * Um usuário autenticado com perfil PACIENTE
         * não pode marcar como lida uma mensagem
         * relacionada ao cadastro de outro paciente.
         *
         * Tópicos:
         * - Testes de Software
         * - Segurança
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void deveBloquearPacienteAoMarcarMensagemDeOutroPacienteComoLida() {

                /*
                 * -------------------------------------------------
                 * 1. MENSAGEM
                 * -------------------------------------------------
                 *
                 * A mensagem 15 existe no sistema.
                 */
                Mensagem mensagem = org.mockito.Mockito.mock(
                                Mensagem.class);

                when(mensagemRepository.findById(15L))
                                .thenReturn(
                                                Optional.of(mensagem));

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE RELACIONADO À MENSAGEM
                 * -------------------------------------------------
                 */
                Paciente pacienteDaMensagem = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioDoPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                when(mensagem.getPaciente())
                                .thenReturn(
                                                pacienteDaMensagem);

                when(pacienteDaMensagem.getUsuario())
                                .thenReturn(
                                                usuarioDoPaciente);

                /*
                 * A mensagem pertence ao usuário 10.
                 */
                when(usuarioDoPaciente.getIdUsuario())
                                .thenReturn(10L);

                /*
                 * -------------------------------------------------
                 * 3. EXECUTA
                 * -------------------------------------------------
                 *
                 * O usuário autenticado é o usuário 6.
                 *
                 * Portanto:
                 *
                 * usuário autenticado = 6
                 * usuário da mensagem = 10
                 *
                 * Como são diferentes, a operação
                 * deve ser bloqueada.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> mensagemService
                                                .marcarComoLidaComAutorizacao(
                                                                15L,
                                                                6L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 4. CONFIRMA A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O paciente só pode marcar como lidas as próprias mensagens.",
                                excecao.getMessage());
        }

        /*
         * Cenário positivo da autorização de mensagens.
         *
         * Um usuário autenticado com perfil PACIENTE
         * pode marcar como lida uma mensagem relacionada
         * ao próprio cadastro.
         *
         * Tópicos:
         * - Testes de Software
         * - Segurança
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Spring Data JPA
         */
        @Test
        void devePermitirPacienteMarcarPropriaMensagemComoLida() {

                /*
                 * -------------------------------------------------
                 * 1. MENSAGEM
                 * -------------------------------------------------
                 *
                 * A mensagem que será marcada como lida.
                 */
                Mensagem mensagem = org.mockito.Mockito.mock(
                                Mensagem.class);

                /*
                 * -------------------------------------------------
                 * 2. PACIENTE DA MENSAGEM
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * A MensagemService verifica:
                 *
                 * mensagem
                 * -> paciente
                 * -> usuário
                 * -> idUsuario
                 */
                when(mensagem.getPaciente())
                                .thenReturn(paciente);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                /*
                 * A mensagem pertence ao usuário 6.
                 *
                 * Esse será também o usuário autenticado.
                 */
                when(usuarioPaciente.getIdUsuario())
                                .thenReturn(6L);

                /*
                 * -------------------------------------------------
                 * 3. BUSCA DA MENSAGEM
                 * -------------------------------------------------
                 *
                 * Atenção:
                 *
                 * marcarComoLidaComAutorizacao()
                 * busca a mensagem uma vez para autorizar.
                 *
                 * Depois chama marcarComoLida(),
                 * que busca novamente pelo mesmo ID.
                 *
                 * O mesmo stub atende às duas chamadas.
                 */
                when(mensagemRepository.findById(15L))
                                .thenReturn(
                                                Optional.of(mensagem));

                /*
                 * -------------------------------------------------
                 * 4. SALVAMENTO
                 * -------------------------------------------------
                 *
                 * Depois de setLida(true), a mensagem
                 * é salva pelo Repository.
                 */
                when(mensagemRepository.save(mensagem))
                                .thenReturn(mensagem);

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA
                 * -------------------------------------------------
                 *
                 * Usuário autenticado:
                 * 6
                 *
                 * Usuário proprietário da mensagem:
                 * 6
                 *
                 * Portanto a operação deve ser permitida.
                 */
                MensagemResponseDTO resposta = mensagemService
                                .marcarComoLidaComAutorizacao(
                                                15L,
                                                6L,
                                                TipoPerfil.PACIENTE);

                /*
                 * -------------------------------------------------
                 * 6. VERIFICA
                 * -------------------------------------------------
                 *
                 * Confirma que a operação foi concluída
                 * e produziu um DTO de resposta.
                 */
                assertNotNull(resposta);

                /*
                 * Esta é a principal alteração
                 * que queremos comprovar.
                 */
                verify(mensagem)
                                .setLida(true);

                /*
                 * Confirma que a alteração foi enviada
                 * para persistência.
                 */
                verify(mensagemRepository)
                                .save(mensagem);
        }

        /*
         * Regra de autorização das mensagens.
         *
         * No modelo atual, o perfil PROFISSIONAL
         * não possui permissão para marcar
         * mensagens como lidas.
         *
         * Tópicos:
         * - Testes de Software
         * - Segurança
         * - Engenharia de Software
         * - Arquitetura de Software
         */
        @Test
        void deveBloquearProfissionalAoMarcarMensagemComoLida() {

                /*
                 * -------------------------------------------------
                 * 1. MENSAGEM EXISTENTE
                 * -------------------------------------------------
                 *
                 * A mensagem precisa existir para que
                 * o fluxo avance até a validação do perfil.
                 */
                Mensagem mensagem = org.mockito.Mockito.mock(
                                Mensagem.class);

                when(mensagemRepository.findById(15L))
                                .thenReturn(
                                                Optional.of(mensagem));

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA
                 * -------------------------------------------------
                 *
                 * O usuário está autenticado como PROFISSIONAL.
                 *
                 * Nesse perfil, a própria Service deve
                 * interromper a operação.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> mensagemService
                                                .marcarComoLidaComAutorizacao(
                                                                15L,
                                                                8L,
                                                                TipoPerfil.PROFISSIONAL));

                /*
                 * -------------------------------------------------
                 * 3. CONFIRMA A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O profissional não possui permissão para marcar mensagens como lidas.",
                                excecao.getMessage());

                /*
                 * Como a operação foi bloqueada,
                 * nenhuma alteração deve ser salva.
                 */
                verify(
                                mensagemRepository,
                                never()).save(
                                                org.mockito.ArgumentMatchers.any());
        }

        /*
         * RF09 - autorização por propriedade.
         *
         * Um paciente autenticado não pode consultar
         * uma mensagem relacionada a outro paciente.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         * - APIs REST
         */
        @Test
        void deveBloquearPacienteAoConsultarMensagemDeOutroPaciente() {

                /*
                 * -------------------------------------------------
                 * 1. MENSAGEM EXISTENTE
                 * -------------------------------------------------
                 */
                Mensagem mensagem = org.mockito.Mockito.mock(
                                Mensagem.class);

                Paciente pacienteDaMensagem = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioDoPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * A mensagem pertence a um paciente
                 * vinculado ao usuário ID 6.
                 */
                when(mensagem.getPaciente())
                                .thenReturn(pacienteDaMensagem);

                when(pacienteDaMensagem.getUsuario())
                                .thenReturn(usuarioDoPaciente);

                when(usuarioDoPaciente.getIdUsuario())
                                .thenReturn(6L);

                /*
                 * A mensagem ID 10 existe.
                 */
                when(mensagemRepository.findById(10L))
                                .thenReturn(
                                                Optional.of(mensagem));

                /*
                 * -------------------------------------------------
                 * 2. EXECUTA COMO OUTRO PACIENTE
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 20
                 * usuário do paciente da mensagem = 6
                 *
                 * Portanto, são pacientes diferentes.
                 */
                IllegalArgumentException excecao = assertThrows(
                                IllegalArgumentException.class,
                                () -> mensagemService
                                                .buscarPorIdComAutorizacao(
                                                                10L,
                                                                20L,
                                                                TipoPerfil.PACIENTE));

                /*
                 * -------------------------------------------------
                 * 3. CONFERE A REGRA
                 * -------------------------------------------------
                 */
                assertEquals(
                                "O paciente só pode consultar as próprias mensagens.",
                                excecao.getMessage());
        }

        /*
         * RF09 - cenário positivo de autorização.
         *
         * Um paciente autenticado pode consultar
         * uma mensagem relacionada ao próprio cadastro.
         *
         * Tópicos:
         * - Testes de Software
         * - Levantamento e Especificação de Requisitos
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         * - APIs REST
         */
        @Test
        void devePermitirPacienteConsultarPropriaMensagem() {

                /*
                 * -------------------------------------------------
                 * 1. PACIENTE DA MENSAGEM
                 * -------------------------------------------------
                 */
                Paciente paciente = org.mockito.Mockito.mock(
                                Paciente.class);

                Usuario usuarioPaciente = org.mockito.Mockito.mock(
                                Usuario.class);

                /*
                 * O paciente está vinculado ao usuário ID 6.
                 */
                when(paciente.getIdPaciente())
                                .thenReturn(4L);

                when(paciente.getUsuario())
                                .thenReturn(usuarioPaciente);

                when(usuarioPaciente.getIdUsuario())
                                .thenReturn(6L);

                when(usuarioPaciente.getNome())
                                .thenReturn("Paciente Teste");

                /*
                 * -------------------------------------------------
                 * 2. USUÁRIO QUE ENVIOU A MENSAGEM
                 * -------------------------------------------------
                 *
                 * converterParaDTO() também utiliza os dados
                 * do usuário associado à mensagem.
                 */
                Usuario usuarioMensagem = org.mockito.Mockito.mock(
                                Usuario.class);

                when(usuarioMensagem.getIdUsuario())
                                .thenReturn(9L);

                when(usuarioMensagem.getNome())
                                .thenReturn("Administrador");

                /*
                 * -------------------------------------------------
                 * 3. MENSAGEM EXISTENTE
                 * -------------------------------------------------
                 */
                Mensagem mensagem = org.mockito.Mockito.mock(
                                Mensagem.class);

                when(mensagem.getPaciente())
                                .thenReturn(paciente);

                when(mensagem.getUsuario())
                                .thenReturn(usuarioMensagem);

                /*
                 * Caso converterParaDTO() utilize o ID,
                 * disponibilizamos também esse valor.
                 */
                when(mensagem.getIdMensagem())
                                .thenReturn(10L);

                /*
                 * -------------------------------------------------
                 * 4. REPOSITORY
                 * -------------------------------------------------
                 *
                 * A mensagem ID 10 existe.
                 */
                when(mensagemRepository.findById(10L))
                                .thenReturn(
                                                Optional.of(mensagem));

                /*
                 * -------------------------------------------------
                 * 5. EXECUTA COMO O PRÓPRIO PACIENTE
                 * -------------------------------------------------
                 *
                 * usuário autenticado = 6
                 * usuário do paciente da mensagem = 6
                 *
                 * Portanto, a consulta deve ser permitida.
                 */
                MensagemResponseDTO resposta = mensagemService
                                .buscarPorIdComAutorizacao(
                                                10L,
                                                6L,
                                                TipoPerfil.PACIENTE);

                /*
                 * -------------------------------------------------
                 * 6. CONFERE O RESULTADO
                 * -------------------------------------------------
                 */
                assertNotNull(resposta);

                /*
                 * Também confirmamos que a mensagem retornada
                 * corresponde ao registro solicitado.
                 */
                assertEquals(
                                10L,
                                resposta.getIdMensagem());
        }

}