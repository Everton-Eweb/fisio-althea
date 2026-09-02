package br.com.althea.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.althea.api.dto.PacienteRequestDTO;
import br.com.althea.api.dto.PacienteResponseDTO;
import br.com.althea.api.entity.Paciente;
import br.com.althea.api.entity.TipoPerfil;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.repository.PacienteRepository;
import br.com.althea.api.repository.UsuarioRepository;

/*
 * Camada de serviço responsável pelas operações
 * relacionadas aos pacientes da Clínica Althea.
 *
 * Fluxo:
 *
 * Controller
 *     ↓
 * PacienteService
 *     ↓
 * PacienteRepository / UsuarioRepository
 *     ↓
 * MySQL
 *
 * Tópicos:
 * - Arquitetura de Software
 * - Programação Orientada a Objetos
 * - Spring Framework
 * - Spring Data JPA
 * - Banco de Dados Relacional
 */
@Service
public class PacienteService {

        /*
         * Repository responsável pelos dados dos pacientes.
         */
        private final PacienteRepository pacienteRepository;

        /*
         * Repository necessário porque o cadastro de paciente
         * também cria um usuário para acesso ao sistema.
         */
        private final UsuarioRepository usuarioRepository;

        /*
         * Componente utilizado para transformar a senha
         * em um hash antes de armazená-la no banco.
         */
        private final PasswordEncoder passwordEncoder;

        /*
         * Injeção de dependência pelo construtor.
         *
         * O Spring fornece automaticamente os três objetos.
         */
        public PacienteService(
                        PacienteRepository pacienteRepository,
                        UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder) {

                this.pacienteRepository = pacienteRepository;
                this.usuarioRepository = usuarioRepository;
                this.passwordEncoder = passwordEncoder;
        }

        /*
         * Lista todos os pacientes.
         *
         * Cada entidade encontrada é convertida para DTO
         * antes de ser retornada pela API.
         */
        public List<PacienteResponseDTO> listarTodos() {

                return pacienteRepository.findAll()
                                .stream()
                                .map(this::converterParaDTO)
                                .toList();
        }

        /*
         * Busca um paciente específico pelo ID.
         */
        public Optional<PacienteResponseDTO> buscarPorId(Long id) {

                return pacienteRepository.findById(id)
                                .map(this::converterParaDTO);
        }

        /*
         * Cadastra um novo paciente.
         *
         * Essa operação envolve duas tabelas:
         *
         * 1. usuario
         * 2. paciente
         *
         * Por isso usamos @Transactional.
         */
        @Transactional
        public PacienteResponseDTO cadastrar(PacienteRequestDTO dados) {

                /*
                 * Antes de cadastrar, verificamos se o e-mail
                 * já está sendo utilizado.
                 */
                if (usuarioRepository.existsByEmail(dados.getEmail())) {

                        throw new IllegalArgumentException(
                                        "Já existe um usuário cadastrado com este e-mail.");
                }

                /*
                 * Primeiro criamos o usuário relacionado
                 * ao novo paciente.
                 */
                Usuario usuario = new Usuario();

                usuario.setNome(dados.getNome());
                usuario.setEmail(dados.getEmail());

                /*
                 * A senha NÃO é armazenada diretamente.
                 *
                 * Exemplo:
                 *
                 * senha recebida:
                 * 123456
                 *
                 * valor salvo:
                 * $2a$10$...
                 */
                usuario.setSenha(
                                passwordEncoder.encode(dados.getSenha()));

                /*
                 * O perfil não é escolhido pelo cliente.
                 *
                 * Como estamos cadastrando um paciente,
                 * o próprio sistema define PACIENTE.
                 */
                usuario.setPerfil(TipoPerfil.PACIENTE);

                /*
                 * Novo usuário começa ativo.
                 */
                usuario.setAtivo(true);

                /*
                 * Salva o usuário primeiro porque precisamos
                 * do id_usuario para relacioná-lo ao paciente.
                 */
                Usuario usuarioSalvo = usuarioRepository.save(usuario);

                /*
                 * Agora criamos o paciente.
                 */
                Paciente paciente = new Paciente();

                /*
                 * Relaciona o paciente ao usuário
                 * recém-criado.
                 */
                paciente.setUsuario(usuarioSalvo);

                paciente.setCpf(dados.getCpf());
                paciente.setDataNascimento(dados.getDataNascimento());
                paciente.setTelefone(dados.getTelefone());
                paciente.setEndereco(dados.getEndereco());

                /*
                 * Salva o paciente no banco.
                 */
                Paciente pacienteSalvo = pacienteRepository.save(paciente);

                /*
                 * A resposta da API utiliza DTO,
                 * portanto nenhuma senha será retornada.
                 */
                return converterParaDTO(pacienteSalvo);
        }

        /*
         * Busca o paciente relacionado ao usuário autenticado.
         *
         * O idUsuario será obtido posteriormente através
         * do token JWT.
         *
         * Dessa forma, o cliente da API não informa
         * qual paciente deseja consultar.
         *
         * O próprio sistema identifica quem está
         * autenticado e retorna somente os seus dados.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Programação Orientada a Objetos
         */
        public PacienteResponseDTO buscarPorUsuario(Long idUsuario) {

                /*
                 * Busca o paciente que possui relacionamento
                 * com o usuário informado.
                 *
                 * Exemplo:
                 *
                 * Usuario
                 * idUsuario = 6
                 *
                 * Paciente
                 * idUsuario = 6
                 */
                Paciente paciente = pacienteRepository
                                .findByUsuario_IdUsuario(idUsuario)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Paciente não encontrado para o usuário autenticado."));

                /*
                 * Não retornamos diretamente a entidade.
                 *
                 * Utilizamos o mesmo DTO já utilizado
                 * pelos outros endpoints de paciente,
                 * evitando expor informações como a senha.
                 */
                return converterParaDTO(paciente);
        }

        /*
         * Converte uma entidade Paciente em
         * PacienteResponseDTO.
         *
         * Dessa forma controlamos quais dados
         * são enviados pela API.
         */
        private PacienteResponseDTO converterParaDTO(Paciente paciente) {

                /*
                 * Caso o paciente não possua usuário relacionado.
                 */
                if (paciente.getUsuario() == null) {

                        return new PacienteResponseDTO(
                                        paciente.getIdPaciente(),
                                        null,
                                        null,
                                        null,
                                        paciente.getCpf(),
                                        paciente.getDataNascimento(),
                                        paciente.getTelefone(),
                                        paciente.getEndereco());
                }

                /*
                 * Paciente com usuário relacionado.
                 *
                 * Observe novamente que senha não é retornada.
                 */
                return new PacienteResponseDTO(
                                paciente.getIdPaciente(),
                                paciente.getUsuario().getIdUsuario(),
                                paciente.getUsuario().getNome(),
                                paciente.getUsuario().getEmail(),
                                paciente.getCpf(),
                                paciente.getDataNascimento(),
                                paciente.getTelefone(),
                                paciente.getEndereco());
        }
}