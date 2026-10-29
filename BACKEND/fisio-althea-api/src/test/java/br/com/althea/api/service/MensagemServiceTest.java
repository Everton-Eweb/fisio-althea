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
import br.com.althea.api.exception.AcessoNegadoException;

import br.com.althea.api.repository.MensagemRepository;
import br.com.althea.api.repository.PacienteRepository;
import br.com.althea.api.repository.UsuarioRepository;

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

@Test
void deveBloquearPacienteAoEnviarMensagemParaOutroPaciente() {

    MensagemRequestDTO dados = new MensagemRequestDTO();

    dados.setIdPaciente(99L);
    dados.setAssunto("Teste");
    dados.setConteudo("Mensagem de teste automatizado");

    Usuario usuarioAutenticado = org.mockito.Mockito.mock(
            Usuario.class);

    when(usuarioRepository.findById(6L))
            .thenReturn(Optional.of(usuarioAutenticado));

    Paciente pacienteInformado = org.mockito.Mockito.mock(
            Paciente.class);

    when(pacienteRepository.findById(99L))
            .thenReturn(Optional.of(pacienteInformado));

    Paciente pacienteAutenticado = org.mockito.Mockito.mock(
            Paciente.class);

    when(pacienteRepository.findByUsuario_IdUsuario(6L))
            .thenReturn(Optional.of(pacienteAutenticado));

    when(pacienteAutenticado.getIdPaciente())
            .thenReturn(4L);

    AcessoNegadoException excecao = assertThrows(
            AcessoNegadoException.class,
            () -> mensagemService.cadastrarComAutorizacao(
                    dados,
                    6L,
                    TipoPerfil.PACIENTE));

    assertEquals(
            "O paciente só pode enviar mensagens relacionadas ao próprio cadastro.",
            excecao.getMessage());

    verify(mensagemRepository, never())
            .saveAndFlush(
                    org.mockito.ArgumentMatchers.any());
}

@Test
void devePermitirPacienteEnviarMensagemDoProprioCadastro() {

    MensagemRequestDTO dados = new MensagemRequestDTO();

    dados.setIdPaciente(4L);
    dados.setAssunto("DÃºvida sobre atendimento");
    dados.setConteudo(
            "Mensagem criada durante o teste automatizado.");

    Usuario usuarioAutenticado = org.mockito.Mockito.mock(
            Usuario.class);

    when(usuarioAutenticado.getIdUsuario()).thenReturn(6L);
    when(usuarioAutenticado.getNome()).thenReturn("Paciente Teste");

    when(usuarioRepository.findById(6L))
            .thenReturn(Optional.of(usuarioAutenticado));

    Paciente paciente = org.mockito.Mockito.mock(Paciente.class);

    when(paciente.getIdPaciente()).thenReturn(4L);
    when(paciente.getUsuario()).thenReturn(usuarioAutenticado);

    when(pacienteRepository.findById(4L))
            .thenReturn(Optional.of(paciente));

    when(pacienteRepository.findByUsuario_IdUsuario(6L))
            .thenReturn(Optional.of(paciente));

    when(mensagemRepository.saveAndFlush(
            org.mockito.ArgumentMatchers.any(Mensagem.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

    MensagemResponseDTO resposta =
            mensagemService.cadastrarComAutorizacao(
                    dados,
                    6L,
                    TipoPerfil.PACIENTE);

    assertNotNull(resposta);

    ArgumentCaptor<Mensagem> captor =
            ArgumentCaptor.forClass(Mensagem.class);

    verify(mensagemRepository).saveAndFlush(captor.capture());

    Mensagem mensagemSalva = captor.getValue();

    assertSame(paciente, mensagemSalva.getPaciente());
    assertSame(usuarioAutenticado, mensagemSalva.getUsuario());

    assertEquals(
            "DÃºvida sobre atendimento",
            mensagemSalva.getAssunto());

    assertEquals(
            "Mensagem criada durante o teste automatizado.",
            mensagemSalva.getConteudo());

    assertFalse(mensagemSalva.getLida());

    verify(entityManager).refresh(mensagemSalva);
}

@Test
void deveBloquearPacienteAoMarcarMensagemDeOutroPacienteComoLida() {

    Mensagem mensagem = org.mockito.Mockito.mock(Mensagem.class);

    when(mensagemRepository.findById(15L))
            .thenReturn(Optional.of(mensagem));

    Paciente pacienteDaMensagem = org.mockito.Mockito.mock(
            Paciente.class);

    Usuario usuarioDoPaciente = org.mockito.Mockito.mock(
            Usuario.class);

    when(mensagem.getPaciente()).thenReturn(pacienteDaMensagem);
    when(pacienteDaMensagem.getUsuario()).thenReturn(usuarioDoPaciente);
    when(usuarioDoPaciente.getIdUsuario()).thenReturn(10L);

    AcessoNegadoException excecao = assertThrows(
            AcessoNegadoException.class,
            () -> mensagemService.marcarComoLidaComAutorizacao(
                    15L,
                    6L,
                    TipoPerfil.PACIENTE));

    assertEquals(
            "O paciente só pode marcar como lidas as próprias mensagens.",
            excecao.getMessage());
}

@Test
void devePermitirPacienteMarcarPropriaMensagemComoLida() {

    Mensagem mensagem = org.mockito.Mockito.mock(Mensagem.class);

    Paciente paciente = org.mockito.Mockito.mock(Paciente.class);

    Usuario usuarioPaciente = org.mockito.Mockito.mock(
            Usuario.class);

    when(mensagem.getPaciente()).thenReturn(paciente);
    when(paciente.getUsuario()).thenReturn(usuarioPaciente);
    when(usuarioPaciente.getIdUsuario()).thenReturn(6L);

    when(mensagemRepository.findById(15L))
            .thenReturn(Optional.of(mensagem));

    when(mensagemRepository.save(mensagem)).thenReturn(mensagem);

    MensagemResponseDTO resposta =
            mensagemService.marcarComoLidaComAutorizacao(
                    15L,
                    6L,
                    TipoPerfil.PACIENTE);

    assertNotNull(resposta);

    verify(mensagem).setLida(true);
    verify(mensagemRepository).save(mensagem);
}

@Test
void deveBloquearProfissionalAoMarcarMensagemComoLida() {

    Mensagem mensagem = org.mockito.Mockito.mock(Mensagem.class);

    when(mensagemRepository.findById(15L))
            .thenReturn(Optional.of(mensagem));

    AcessoNegadoException excecao = assertThrows(
            AcessoNegadoException.class,
            () -> mensagemService.marcarComoLidaComAutorizacao(
                    15L,
                    8L,
                    TipoPerfil.PROFISSIONAL));

    assertEquals(
            "O profissional não possui permissão para marcar mensagens como lidas.",
            excecao.getMessage());

    verify(mensagemRepository, never())
            .save(org.mockito.ArgumentMatchers.any());
}

@Test
void deveBloquearPacienteAoConsultarMensagemDeOutroPaciente() {

    Mensagem mensagem = org.mockito.Mockito.mock(Mensagem.class);

    Paciente pacienteDaMensagem = org.mockito.Mockito.mock(
            Paciente.class);

    Usuario usuarioDoPaciente = org.mockito.Mockito.mock(
            Usuario.class);

    when(mensagem.getPaciente()).thenReturn(pacienteDaMensagem);
    when(pacienteDaMensagem.getUsuario()).thenReturn(usuarioDoPaciente);
    when(usuarioDoPaciente.getIdUsuario()).thenReturn(6L);

    when(mensagemRepository.findById(10L))
            .thenReturn(Optional.of(mensagem));

    AcessoNegadoException excecao = assertThrows(
            AcessoNegadoException.class,
            () -> mensagemService.buscarPorIdComAutorizacao(
                    10L,
                    20L,
                    TipoPerfil.PACIENTE));

    assertEquals(
            "O paciente só pode consultar as próprias mensagens.",
            excecao.getMessage());
}

@Test
void devePermitirPacienteConsultarPropriaMensagem() {

    Paciente paciente = org.mockito.Mockito.mock(Paciente.class);

    Usuario usuarioPaciente = org.mockito.Mockito.mock(
            Usuario.class);

    when(paciente.getIdPaciente()).thenReturn(4L);
    when(paciente.getUsuario()).thenReturn(usuarioPaciente);
    when(usuarioPaciente.getIdUsuario()).thenReturn(6L);
    when(usuarioPaciente.getNome()).thenReturn("Paciente Teste");

    Usuario usuarioMensagem = org.mockito.Mockito.mock(
            Usuario.class);

    when(usuarioMensagem.getIdUsuario()).thenReturn(9L);
    when(usuarioMensagem.getNome()).thenReturn("Administrador");

    Mensagem mensagem = org.mockito.Mockito.mock(Mensagem.class);

    when(mensagem.getPaciente()).thenReturn(paciente);
    when(mensagem.getUsuario()).thenReturn(usuarioMensagem);
    when(mensagem.getIdMensagem()).thenReturn(10L);

    when(mensagemRepository.findById(10L))
            .thenReturn(Optional.of(mensagem));

    MensagemResponseDTO resposta =
            mensagemService.buscarPorIdComAutorizacao(
                    10L,
                    6L,
                    TipoPerfil.PACIENTE);

    assertNotNull(resposta);
    assertEquals(10L, resposta.getIdMensagem());
}

}
