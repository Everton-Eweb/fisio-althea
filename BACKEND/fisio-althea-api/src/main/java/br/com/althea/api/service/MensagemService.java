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

/*
 * Camada de serviço responsável pelas operações
 * relacionadas às mensagens do sistema.
 *
 * Fluxo:
 *
 * Controller
 *      ↓
 * MensagemService
 *      ↓
 * Repositories
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
public class MensagemService {

        /*
         * Repository principal utilizado para
         * consultar e salvar mensagens.
         */
        private final MensagemRepository mensagemRepository;

        /*
         * EntityManager permite sincronizar novamente
         * a entidade Java com os valores gerados pelo banco.
         *
         * Isso será utilizado principalmente para recuperar
         * o data_envio preenchido pelo CURRENT_TIMESTAMP.
         */
        private final EntityManager entityManager;

        /*
         * Repositories utilizados para validar
         * os relacionamentos da mensagem.
         */
        private final PacienteRepository pacienteRepository;
        private final UsuarioRepository usuarioRepository;

        /*
         * Injeção de dependência pelo construtor.
         *
         * O Spring fornece automaticamente
         * as implementações dos repositories.
         */
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
         *
         * Cada entidade é convertida para DTO
         * antes de ser enviada pela API.
         */
        public List<MensagemResponseDTO> listarTodas() {

                return mensagemRepository.findAll()
                                .stream()
                                .map(this::converterParaDTO)
                                .toList();
        }

        /*
         * Busca uma mensagem específica pelo ID.
         */
        public Optional<MensagemResponseDTO> buscarPorId(Long id) {

                return mensagemRepository.findById(id)
                                .map(this::converterParaDTO);
        }

        /*
         * Marca uma mensagem como lida considerando
         * o usuário e o perfil atualmente autenticados.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode marcar qualquer mensagem.
         *
         * RECEPCIONISTA
         * -> pode marcar qualquer mensagem.
         *
         * PACIENTE
         * -> somente mensagens relacionadas
         * ao próprio cadastro.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public MensagemResponseDTO marcarComoLidaComAutorizacao(
                        Long id,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA A MENSAGEM
                 * -------------------------------------------------
                 */
                Mensagem mensagem = mensagemRepository
                                .findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Mensagem não encontrada."));

                /*
                 * -------------------------------------------------
                 * 2. VALIDA O PACIENTE
                 * -------------------------------------------------
                 *
                 * Caso o usuário autenticado seja PACIENTE,
                 * verificamos se a mensagem realmente
                 * está relacionada ao cadastro dele.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        Long idUsuarioDoPaciente = mensagem
                                        .getPaciente()
                                        .getUsuario()
                                        .getIdUsuario();

                        /*
                         * Compara:
                         *
                         * usuário do JWT
                         * X
                         * usuário do paciente da mensagem
                         */
                        if (!idUsuarioAutenticado.equals(idUsuarioDoPaciente)) {

                                throw new IllegalArgumentException(
                                                "O paciente só pode marcar como lidas as próprias mensagens.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 3. BLOQUEIA PROFISSIONAL
                 * -------------------------------------------------
                 *
                 * Nosso modelo atual não possui um destinatário
                 * explícito na entidade Mensagem.
                 *
                 * Portanto ainda não é possível determinar
                 * corretamente quais mensagens um profissional
                 * poderia marcar como lidas.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        throw new IllegalArgumentException(
                                        "O profissional não possui permissão para marcar mensagens como lidas.");
                }

                /*
                 * -------------------------------------------------
                 * 4. MARCA COMO LIDA
                 * -------------------------------------------------
                 *
                 * ADMINISTRADOR e RECEPCIONISTA chegam
                 * diretamente até aqui.
                 *
                 * PACIENTE chega somente se a mensagem
                 * estiver relacionada ao próprio cadastro.
                 *
                 * Reutilizamos o método já existente.
                 */
                return marcarComoLida(id);
        }

        /*
         * Marca uma mensagem como lida.
         *
         * A mensagem é localizada pelo ID e,
         * caso exista, o campo lida é alterado
         * de false para true.
         *
         * Tópicos:
         * - Programação Orientada a Objetos
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public MensagemResponseDTO marcarComoLida(Long id) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA A MENSAGEM
                 * -------------------------------------------------
                 *
                 * Caso o ID informado não exista,
                 * interrompemos a operação.
                 */
                Mensagem mensagem = mensagemRepository
                                .findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Mensagem não encontrada."));

                /*
                 * -------------------------------------------------
                 * 2. ALTERA O ESTADO DA MENSAGEM
                 * -------------------------------------------------
                 *
                 * Antes:
                 * lida = false
                 *
                 * Depois:
                 * lida = true
                 */
                mensagem.setLida(true);

                /*
                 * -------------------------------------------------
                 * 3. SALVA A ALTERAÇÃO
                 * -------------------------------------------------
                 */
                Mensagem mensagemAtualizada = mensagemRepository.save(mensagem);

                /*
                 * Retorna a mensagem atualizada utilizando
                 * o DTO de resposta.
                 */
                return converterParaDTO(mensagemAtualizada);
        }

        /*
         * Retorna somente as mensagens relacionadas
         * ao paciente vinculado ao usuário autenticado.
         *
         * O idUsuario será obtido posteriormente
         * diretamente do token JWT.
         *
         * Dessa forma, o paciente não informa
         * qual idPaciente deseja consultar.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Programação Orientada a Objetos
         */
        public List<MensagemResponseDTO> listarMinhasMensagens(
                        Long idUsuarioAutenticado) {

                /*
                 * Busca somente as mensagens cujo paciente
                 * está relacionado ao usuário informado.
                 *
                 * Fluxo:
                 *
                 * Usuario
                 * ↓
                 * Paciente
                 * ↓
                 * Mensagens
                 */
                return mensagemRepository
                                .findByPaciente_Usuario_IdUsuario(
                                                idUsuarioAutenticado)
                                .stream()
                                .map(this::converterParaDTO)
                                .toList();
        }

        /*
         * Cadastra uma mensagem considerando
         * o usuário atualmente autenticado.
         *
         * Regras principais:
         *
         * - o remetente NÃO é definido pelo JSON;
         * - o remetente é obtido através do idUsuario do JWT;
         *
         * PACIENTE:
         * só pode enviar mensagem relacionada
         * ao próprio cadastro de paciente.
         *
         * ADMINISTRADOR, RECEPCIONISTA e PROFISSIONAL:
         * podem enviar mensagem relacionada
         * a um paciente existente.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         */
        @Transactional
        public MensagemResponseDTO cadastrarComAutorizacao(
                        MensagemRequestDTO dados,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * -------------------------------------------------
                 * 1. LOCALIZA O USUÁRIO AUTENTICADO
                 * -------------------------------------------------
                 *
                 * Não utilizamos o idUsuario enviado no JSON.
                 *
                 * O remetente é determinado pelo JWT.
                 */
                Usuario usuarioAutenticado = usuarioRepository
                                .findById(idUsuarioAutenticado)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Usuário autenticado não encontrado."));

                /*
                 * -------------------------------------------------
                 * 2. LOCALIZA O PACIENTE INFORMADO
                 * -------------------------------------------------
                 */
                Paciente paciente = pacienteRepository
                                .findById(dados.getIdPaciente())
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Paciente não encontrado."));

                /*
                 * -------------------------------------------------
                 * 3. VALIDA O PACIENTE AUTENTICADO
                 * -------------------------------------------------
                 *
                 * Se quem está enviando a mensagem é PACIENTE,
                 * ele somente pode relacionar a mensagem
                 * ao próprio cadastro.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        Paciente pacienteAutenticado = pacienteRepository
                                        .findByUsuario_IdUsuario(
                                                        idUsuarioAutenticado)
                                        .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                        "Paciente não encontrado para o usuário autenticado."));

                        /*
                         * Compara o paciente do JWT
                         * com o paciente informado no JSON.
                         */
                        if (!pacienteAutenticado
                                        .getIdPaciente()
                                        .equals(dados.getIdPaciente())) {

                                throw new IllegalArgumentException(
                                                "O paciente só pode enviar mensagens relacionadas ao próprio cadastro.");
                        }
                }

                /*
                 * -------------------------------------------------
                 * 4. CRIA A MENSAGEM
                 * -------------------------------------------------
                 */
                Mensagem mensagem = new Mensagem();

                mensagem.setPaciente(paciente);

                /*
                 * IMPORTANTE:
                 *
                 * O usuário relacionado à mensagem vem do JWT,
                 * e não de dados enviados pelo cliente.
                 */
                mensagem.setUsuario(usuarioAutenticado);

                mensagem.setAssunto(dados.getAssunto());
                mensagem.setConteudo(dados.getConteudo());

                /*
                 * Toda mensagem nova começa como não lida.
                 */
                mensagem.setLida(false);

                /*
                 * -------------------------------------------------
                 * 5. SALVA E ATUALIZA OS VALORES DO BANCO
                 * -------------------------------------------------
                 *
                 * saveAndFlush força o INSERT.
                 *
                 * refresh recupera data_envio,
                 * preenchida pelo CURRENT_TIMESTAMP.
                 */
                Mensagem mensagemSalva = mensagemRepository.saveAndFlush(mensagem);

                entityManager.refresh(mensagemSalva);

                /*
                 * -------------------------------------------------
                 * 6. RETORNA O DTO
                 * -------------------------------------------------
                 */
                return converterParaDTO(mensagemSalva);
        }

        /*
         * Consulta uma mensagem específica considerando
         * o usuário e o perfil autenticados.
         *
         * ADMINISTRADOR e RECEPCIONISTA:
         * podem consultar qualquer mensagem.
         *
         * PACIENTE:
         * pode consultar somente mensagens
         * relacionadas ao próprio cadastro.
         *
         * PROFISSIONAL:
         * permanece bloqueado neste momento,
         * pois o modelo atual não possui um
         * destinatário explícito da mensagem.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - APIs REST
         */
        public MensagemResponseDTO buscarPorIdComAutorizacao(
                        Long id,
                        Long idUsuarioAutenticado,
                        TipoPerfil perfilAutenticado) {

                /*
                 * 1. Localiza a mensagem solicitada.
                 */
                Mensagem mensagem = mensagemRepository
                                .findById(id)
                                .orElseThrow(() -> new RecursoNaoEncontradoException(
                                                "Mensagem não encontrada."));

                /*
                 * 2. Caso seja PACIENTE, verificamos
                 * se a mensagem pertence ao seu cadastro.
                 */
                if (perfilAutenticado == TipoPerfil.PACIENTE) {

                        Long idUsuarioDoPaciente = mensagem
                                        .getPaciente()
                                        .getUsuario()
                                        .getIdUsuario();

                        if (!idUsuarioAutenticado.equals(idUsuarioDoPaciente)) {

                                throw new IllegalArgumentException(
                                                "O paciente só pode consultar as próprias mensagens.");
                        }
                }

                /*
                 * 3. O PROFISSIONAL ainda não possui
                 * consulta individual neste modelo.
                 */
                if (perfilAutenticado == TipoPerfil.PROFISSIONAL) {

                        throw new IllegalArgumentException(
                                        "O profissional não possui permissão para consultar esta mensagem.");
                }

                /*
                 * ADMINISTRADOR e RECEPCIONISTA passam
                 * diretamente.
                 *
                 * PACIENTE chega aqui somente após
                 * a validação de propriedade.
                 */
                return converterParaDTO(mensagem);
        }

        /*
         * Converte a entidade Mensagem
         * para MensagemResponseDTO.
         *
         * Assim não devolvemos diretamente
         * as entidades Paciente e Usuario.
         */
        private MensagemResponseDTO converterParaDTO(
                        Mensagem mensagem) {

                /*
                 * Recupera os dados do paciente.
                 */
                Long idPaciente = null;
                String nomePaciente = null;

                if (mensagem.getPaciente() != null) {

                        idPaciente = mensagem
                                        .getPaciente()
                                        .getIdPaciente();

                        /*
                         * O nome está no Usuario
                         * relacionado ao Paciente.
                         */
                        if (mensagem.getPaciente().getUsuario() != null) {

                                nomePaciente = mensagem
                                                .getPaciente()
                                                .getUsuario()
                                                .getNome();
                        }
                }

                /*
                 * Recupera os dados do usuário
                 * diretamente relacionado à mensagem.
                 */
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

                /*
                 * Monta o DTO que será enviado
                 * como resposta da API.
                 */
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