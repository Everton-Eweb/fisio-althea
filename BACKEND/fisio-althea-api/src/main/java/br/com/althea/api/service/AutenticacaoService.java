package br.com.althea.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.althea.api.dto.LoginRequestDTO;
import br.com.althea.api.dto.LoginResponseDTO;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.repository.UsuarioRepository;

/*
 * Serviço responsável pela autenticação
 * dos usuários do sistema.
 *
 * Fluxo:
 *
 * LoginController
 *       ↓
 * AutenticacaoService
 *       ↓
 * UsuarioRepository
 *       ↓
 * MySQL
 *
 * Depois de localizar o usuário, a senha
 * informada é comparada com o hash BCrypt.
 *
 * Tópicos:
 * - Segurança
 * - Spring Framework
 * - Spring Data JPA
 * - Arquitetura de Software
 * - Programação Orientada a Objetos
 */
@Service
public class AutenticacaoService {

    /*
     * Repository utilizado para localizar
     * o usuário pelo e-mail.
     */
    private final UsuarioRepository usuarioRepository;

    /*
     * Serviço responsável pela geração
     * do token JWT após o login.
     */
    private final JwtService jwtService;

    /*
     * PasswordEncoder já configurado no
     * PasswordConfig com BCrypt.
     *
     * Ele será utilizado para comparar
     * a senha digitada com o hash armazenado.
     */
    private final PasswordEncoder passwordEncoder;

    /*
     * Injeção de dependência pelo construtor.
     */
    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /*
     * Realiza a autenticação do usuário.
     *
     * Recebe:
     * - e-mail;
     * - senha.
     *
     * Retorna os dados básicos do usuário
     * quando as credenciais estão corretas.
     */
    public LoginResponseDTO autenticar(LoginRequestDTO dados) {

        /*
         * -------------------------------------------------
         * 1. LOCALIZA O USUÁRIO PELO E-MAIL
         * -------------------------------------------------
         *
         * Caso o e-mail não esteja cadastrado,
         * retornamos uma mensagem genérica.
         *
         * Evitamos informar explicitamente
         * se determinado e-mail existe ou não.
         */
        Usuario usuario = usuarioRepository
                .findByEmail(dados.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "E-mail ou senha inválidos."));

        /*
         * -------------------------------------------------
         * 2. VERIFICA SE O USUÁRIO ESTÁ ATIVO
         * -------------------------------------------------
         *
         * Um usuário inativo não deve conseguir
         * autenticar no sistema.
         */
        if (!Boolean.TRUE.equals(usuario.getAtivo())) {

            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos.");
        }

        /*
         * -------------------------------------------------
         * 3. COMPARA A SENHA COM O HASH BCRYPT
         * -------------------------------------------------
         *
         * IMPORTANTE:
         *
         * Não fazemos:
         *
         * dados.getSenha().equals(usuario.getSenha())
         *
         * porque no banco não está armazenada
         * a senha original.
         *
         * PasswordEncoder.matches() recebe:
         *
         * 1º parâmetro -> senha digitada;
         * 2º parâmetro -> hash salvo no banco.
         */
        boolean senhaCorreta = passwordEncoder.matches(
                dados.getSenha(),
                usuario.getSenha());

        /*
         * Se a senha não corresponder ao hash
         * armazenado, a autenticação é rejeitada.
         */
        if (!senhaCorreta) {

            throw new IllegalArgumentException(
                    "E-mail ou senha inválidos.");
        }

        /*
         * -------------------------------------------------
         * 4. GERA O TOKEN JWT
         * -------------------------------------------------
         *
         * Neste ponto:
         *
         * - usuário existe;
         * - usuário está ativo;
         * - senha está correta.
         *
         * Portanto podemos gerar o token.
         */
        String token = jwtService.gerarToken(usuario);

        /*
         * -------------------------------------------------
         * 5. RETORNA O LOGIN
         * -------------------------------------------------
         *
         * A resposta agora inclui o token JWT.
         */
        return new LoginResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                token);
    }
}