package br.com.althea.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.althea.api.dto.ProfissionalRequestDTO;
import br.com.althea.api.dto.ProfissionalResponseDTO;
import br.com.althea.api.entity.Profissional;
import br.com.althea.api.entity.TipoPerfil;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.repository.ProfissionalRepository;
import br.com.althea.api.repository.UsuarioRepository;

/*
 * Camada de serviço responsável pelas operações
 * relacionadas aos profissionais da Clínica Althea.
 *
 * Fluxo:
 *
 * Controller
 *     ↓
 * ProfissionalService
 *     ↓
 * ProfissionalRepository / UsuarioRepository
 *     ↓
 * Banco MySQL
 *
 * Tópicos:
 * - Arquitetura de Software
 * - Spring Framework
 * - Spring Data JPA
 * - Programação Orientada a Objetos
 * - Banco de Dados Relacional
 */
@Service
public class ProfissionalService {

        /*
         * Repository responsável pelos dados
         * específicos dos profissionais.
         */
        private final ProfissionalRepository profissionalRepository;

        /*
         * Repository utilizado para criar e consultar
         * o usuário relacionado ao profissional.
         */
        private final UsuarioRepository usuarioRepository;

        /*
         * Componente responsável por transformar
         * a senha em hash antes de salvá-la.
         */
        private final PasswordEncoder passwordEncoder;

        /*
         * Injeção de dependência pelo construtor.
         *
         * O Spring fornece automaticamente
         * as implementações necessárias.
         */
        public ProfissionalService(
                        ProfissionalRepository profissionalRepository,
                        UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder) {

                this.profissionalRepository = profissionalRepository;
                this.usuarioRepository = usuarioRepository;
                this.passwordEncoder = passwordEncoder;
        }

        /*
         * Retorna todos os profissionais cadastrados.
         *
         * Cada entidade Profissional é convertida
         * para ProfissionalResponseDTO antes de ser
         * devolvida pela API.
         */
        public List<ProfissionalResponseDTO> listarTodos() {

                return profissionalRepository.findAll()
                                .stream()
                                .map(this::converterParaDTO)
                                .toList();
        }

        /*
         * Busca um profissional pelo seu ID.
         *
         * Optional é utilizado porque o profissional
         * pode existir ou não no banco.
         */
        public Optional<ProfissionalResponseDTO> buscarPorId(Long id) {

                return profissionalRepository.findById(id)
                                .map(this::converterParaDTO);
        }

        /*
         * Cadastra um novo profissional.
         *
         * A operação envolve duas tabelas:
         *
         * 1. usuario
         * 2. profissional
         *
         * Por isso usamos @Transactional.
         */
        @Transactional
        public ProfissionalResponseDTO cadastrar(
                        ProfissionalRequestDTO dados) {

                /*
                 * Verifica se o e-mail informado
                 * já está sendo utilizado.
                 */
                if (usuarioRepository.existsByEmail(dados.getEmail())) {

                        throw new IllegalArgumentException(
                                        "Já existe um usuário cadastrado com este e-mail.");
                }

                /*
                 * Primeiro criamos o usuário
                 * relacionado ao profissional.
                 */
                Usuario usuario = new Usuario();

                usuario.setNome(dados.getNome());
                usuario.setEmail(dados.getEmail());

                /*
                 * A senha nunca deve ser salva
                 * diretamente no banco.
                 *
                 * O PasswordEncoder gera o hash BCrypt.
                 */
                usuario.setSenha(
                                passwordEncoder.encode(dados.getSenha()));

                /*
                 * O próprio sistema define o perfil.
                 *
                 * Quem utiliza este endpoint não poderá
                 * escolher outro perfil manualmente.
                 */
                usuario.setPerfil(TipoPerfil.PROFISSIONAL);

                /*
                 * Todo novo usuário profissional
                 * começa ativo.
                 */
                usuario.setAtivo(true);

                /*
                 * Salva primeiro o usuário,
                 * gerando o id_usuario.
                 */
                Usuario usuarioSalvo = usuarioRepository.save(usuario);

                /*
                 * Agora criamos o profissional.
                 */
                Profissional profissional = new Profissional();

                /*
                 * Relaciona o profissional ao usuário
                 * que acabamos de cadastrar.
                 */
                profissional.setUsuario(usuarioSalvo);

                profissional.setCrefito(dados.getCrefito());
                profissional.setEspecialidade(dados.getEspecialidade());
                profissional.setTelefone(dados.getTelefone());

                /*
                 * Salva o profissional no banco.
                 */
                Profissional profissionalSalvo = profissionalRepository.save(profissional);

                /*
                 * Converte para DTO antes de devolver.
                 *
                 * A senha não aparece na resposta.
                 */
                return converterParaDTO(profissionalSalvo);
        }

        /*
         * Busca o profissional relacionado
         * ao usuário autenticado.
         *
         * O idUsuario será obtido posteriormente
         * diretamente do token JWT.
         *
         * Dessa forma, o usuário não escolhe
         * qual profissional deseja consultar.
         *
         * O sistema identifica automaticamente
         * o profissional vinculado ao usuário logado.
         *
         * Tópicos:
         * - Segurança
         * - Arquitetura de Software
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Programação Orientada a Objetos
         */
        public ProfissionalResponseDTO buscarPorUsuario(Long idUsuario) {

                /*
                 * Busca o profissional que possui
                 * relacionamento com o usuário informado.
                 *
                 * Exemplo:
                 *
                 * Usuario
                 * idUsuario = 8
                 *
                 * Profissional
                 * idUsuario = 8
                 */
                Profissional profissional = profissionalRepository
                                .findByUsuario_IdUsuario(idUsuario)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Profissional não encontrado para o usuário autenticado."));

                /*
                 * Retorna um DTO em vez da entidade.
                 *
                 * Assim não expomos dados internos
                 * do Usuario, principalmente a senha.
                 */
                return converterParaDTO(profissional);
        }

        /*
         * Método auxiliar responsável por converter
         * Profissional em ProfissionalResponseDTO.
         */
        private ProfissionalResponseDTO converterParaDTO(
                        Profissional profissional) {

                /*
                 * Caso não exista usuário relacionado,
                 * evitamos NullPointerException.
                 */
                if (profissional.getUsuario() == null) {

                        return new ProfissionalResponseDTO(
                                        profissional.getIdProfissional(),
                                        null,
                                        null,
                                        null,
                                        profissional.getCrefito(),
                                        profissional.getEspecialidade(),
                                        profissional.getTelefone());
                }

                /*
                 * Caso exista usuário relacionado,
                 * retornamos somente os dados necessários.
                 *
                 * A senha não é incluída.
                 */
                return new ProfissionalResponseDTO(
                                profissional.getIdProfissional(),
                                profissional.getUsuario().getIdUsuario(),
                                profissional.getUsuario().getNome(),
                                profissional.getUsuario().getEmail(),
                                profissional.getCrefito(),
                                profissional.getEspecialidade(),
                                profissional.getTelefone());
        }
}