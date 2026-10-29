package br.com.althea.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.althea.api.dto.MensagemRequestDTO;
import br.com.althea.api.dto.MensagemResponseDTO;

import br.com.althea.api.entity.Mensagem;
import br.com.althea.api.entity.Paciente;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.entity.TipoPerfil;

import jakarta.persistence.EntityManager;

import br.com.althea.api.repository.MensagemRepository;
import br.com.althea.api.repository.PacienteRepository;
import br.com.althea.api.repository.UsuarioRepository;

import br.com.althea.api.exception.RecursoNaoEncontradoException;
import br.com.althea.api.exception.AcessoNegadoException;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final EntityManager entityManager;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public MensagemService(
            MensagemRepository mensagemRepository,
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository,
            EntityManager entityManager) {

        this.mensagemRepository = mensagemRepository;
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.entityManager = entityManager;
    }

    /*
     * Retorna todas as mensagens cadastradas.
     */
    public List<MensagemResponseDTO> listarTodas() {

        return mensagemRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /*
     * Busca uma mensagem pelo ID.
     */
    public Optional<MensagemResponseDTO> buscarPorId(Long id) {

        return mensagemRepository.findById(id)
                .map(this::converterParaDTO);
    }

    /*
     * Marca uma mensagem como lida, respeitando
     * as permissões do usuário autenticado.
     *
     * ADMINISTRADOR e RECEPCIONISTA:
     * podem marcar qualquer mensagem.
     *
     * PACIENTE:
     * pode marcar somente as próprias mensagens.
     *
     * PROFISSIONAL:
     * não pode marcar mensagens como lidas
     * neste modelo atual.
     */
    @Transactional
    public MensagemResponseDTO marcarComoLidaComAutorizacao(
            Long id,
            Long idUsuarioAutenticado,
            TipoPerfil perfilAutenticado) {

        Mensagem mensagem = mensagemRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Mensagem não encontrada."));

        /*
         * Valida se a mensagem pertence ao paciente
         * autenticado.
         */
        if (perfilAutenticado == TipoPerfil.PACIENTE) {

            Long idUsuarioDoPaciente = mensagem
                    .getPaciente()
                    .getUsuario()
                    .getIdUsuario();

            if (!idUsuarioAutenticado.equals(idUsuarioDoPaciente)) {

                throw new AcessoNegadoException(
                        "O paciente só pode marcar como lidas as próprias mensagens.");
            }
        }

        /*
         * Profissionais não podem marcar mensagens
         * como lidas neste modelo.
         */
        if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

            throw new AcessoNegadoException(
                    "O profissional não possui permissão para marcar mensagens como lidas.");
        }

        return marcarComoLida(id);
    }

    /*
     * Marca uma mensagem como lida.
     */
    @Transactional
    public MensagemResponseDTO marcarComoLida(Long id) {

        Mensagem mensagem = mensagemRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Mensagem não encontrada."));

        mensagem.setLida(true);

        Mensagem mensagemAtualizada = mensagemRepository.save(mensagem);

        return converterParaDTO(mensagemAtualizada);
    }

    /*
     * Lista somente as mensagens relacionadas
     * ao paciente vinculado ao usuário autenticado.
     */
    public List<MensagemResponseDTO> listarMinhasMensagens(
            Long idUsuarioAutenticado) {

        return mensagemRepository
                .findByPaciente_Usuario_IdUsuario(idUsuarioAutenticado)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    /*
     * Cadastra uma mensagem respeitando as permissões.
     *
     * O remetente é obtido do JWT, não do JSON.
     *
     * PACIENTE:
     * só pode enviar mensagens relacionadas
     * ao próprio cadastro.
     *
     * ADMINISTRADOR, RECEPCIONISTA e PROFISSIONAL:
     * podem enviar mensagens relacionadas
     * a um paciente existente.
     */
    @Transactional
    public MensagemResponseDTO cadastrarComAutorizacao(
            MensagemRequestDTO dados,
            Long idUsuarioAutenticado,
            TipoPerfil perfilAutenticado) {

        /*
         * Localiza o usuário autenticado.
         *
         * Mantemos IllegalArgumentException neste caso,
         * pois não se trata de uma verificação de
         * propriedade de mensagem.
         */
        Usuario usuarioAutenticado = usuarioRepository
                .findById(idUsuarioAutenticado)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuário autenticado não encontrado."));

        /*
         * Localiza o paciente informado.
         */
        Paciente paciente = pacienteRepository
                .findById(dados.getIdPaciente())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Paciente não encontrado."));

        /*
         * Se o remetente for PACIENTE, verifica
         * se ele está enviando a mensagem para
         * o próprio cadastro.
         */
        if (perfilAutenticado == TipoPerfil.PACIENTE) {

            Paciente pacienteAutenticado = pacienteRepository
                    .findByUsuario_IdUsuario(idUsuarioAutenticado)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Paciente não encontrado para o usuário autenticado."));

            if (!pacienteAutenticado
                    .getIdPaciente()
                    .equals(dados.getIdPaciente())) {

                throw new AcessoNegadoException(
                        "O paciente só pode enviar mensagens relacionadas ao próprio cadastro.");
            }
        }

        /*
         * Cria a mensagem.
         */
        Mensagem mensagem = new Mensagem();

        mensagem.setPaciente(paciente);

        /*
         * O remetente vem do usuário autenticado pelo JWT.
         */
        mensagem.setUsuario(usuarioAutenticado);

        mensagem.setAssunto(dados.getAssunto());
        mensagem.setConteudo(dados.getConteudo());

        /*
         * Toda mensagem nova começa como não lida.
         */
        mensagem.setLida(false);

        /*
         * Salva a mensagem e atualiza os valores
         * gerados pelo banco, incluindo data_envio.
         */
        Mensagem mensagemSalva = mensagemRepository.saveAndFlush(mensagem);

        entityManager.refresh(mensagemSalva);

        return converterParaDTO(mensagemSalva);
    }

    /*
     * Consulta uma mensagem específica respeitando
     * as permissões do usuário autenticado.
     *
     * ADMINISTRADOR e RECEPCIONISTA:
     * podem consultar qualquer mensagem.
     *
     * PACIENTE:
     * pode consultar somente as próprias mensagens.
     *
     * PROFISSIONAL:
     * não pode consultar mensagens individualmente
     * neste modelo atual.
     */
    public MensagemResponseDTO buscarPorIdComAutorizacao(
            Long id,
            Long idUsuarioAutenticado,
            TipoPerfil perfilAutenticado) {

        Mensagem mensagem = mensagemRepository
                .findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Mensagem não encontrada."));

        /*
         * Verifica se a mensagem pertence ao paciente.
         */
        if (perfilAutenticado == TipoPerfil.PACIENTE) {

            Long idUsuarioDoPaciente = mensagem
                    .getPaciente()
                    .getUsuario()
                    .getIdUsuario();

            if (!idUsuarioAutenticado.equals(idUsuarioDoPaciente)) {

                throw new AcessoNegadoException(
                        "O paciente só pode consultar as próprias mensagens.");
            }
        }

        /*
         * Bloqueia a consulta individual pelo profissional.
         */
        if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

            throw new AcessoNegadoException(
                    "O profissional não possui permissão para consultar esta mensagem.");
        }

        return converterParaDTO(mensagem);
    }

    /*
     * Converte a entidade Mensagem para DTO.
     */
    private MensagemResponseDTO converterParaDTO(Mensagem mensagem) {

        Long idPaciente = null;
        String nomePaciente = null;

        if (mensagem.getPaciente() != null) {

            idPaciente = mensagem
                    .getPaciente()
                    .getIdPaciente();

            if (mensagem.getPaciente().getUsuario() != null) {

                nomePaciente = mensagem
                        .getPaciente()
                        .getUsuario()
                        .getNome();
            }
        }

        Long idUsuario = null;
        String nomeUsuario = null;

        if (mensagem.getUsuario() != null) {

            idUsuario = mensagem
                    .getUsuario()
                    .getIdUsuario();

            nomeUsuario = mensagem
                    .getUsuario()
                    .getNome();
        }

        return new MensagemResponseDTO(
                mensagem.getIdMensagem(),
                idPaciente,
                nomePaciente,
                idUsuario,
                nomeUsuario,
                mensagem.getAssunto(),
                mensagem.getConteudo(),
                mensagem.getDataEnvio(),
                mensagem.getLida());
    }
}