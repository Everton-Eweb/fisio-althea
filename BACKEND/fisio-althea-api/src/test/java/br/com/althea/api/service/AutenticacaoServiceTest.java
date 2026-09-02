package br.com.althea.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.althea.api.dto.LoginRequestDTO;
import br.com.althea.api.dto.LoginResponseDTO;

import br.com.althea.api.entity.Usuario;
import br.com.althea.api.entity.TipoPerfil;

import br.com.althea.api.repository.UsuarioRepository;

/*
 * Testes unitários da autenticação.
 *
 * Os repositories e componentes de segurança
 * são simulados com Mockito.
 *
 * Nenhum dado real do banco é alterado.
 *
 * Tópicos:
 * - Testes de Software
 * - Segurança
 * - Spring Security
 * - Engenharia de Software
 */
@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    /*
     * Cria automaticamente a AutenticacaoService
     * utilizando os mocks definidos acima.
     */
    @InjectMocks
    private AutenticacaoService autenticacaoService;

    /*
     * Deve bloquear a autenticação
     * quando a senha informada estiver incorreta.
     */
    @Test
    void deveBloquearLoginComSenhaIncorreta() {

        /*
         * -------------------------------------------------
         * 1. DADOS INFORMADOS NO LOGIN
         * -------------------------------------------------
         */
        LoginRequestDTO dados = new LoginRequestDTO();

        dados.setEmail(
                "paciente@teste.com");

        dados.setSenha(
                "senha-incorreta");

        /*
         * -------------------------------------------------
         * 2. USUÁRIO SIMULADO
         * -------------------------------------------------
         */
        Usuario usuario = org.mockito.Mockito.mock(
                Usuario.class);

        when(usuario.getSenha())
                .thenReturn(
                        "$2a$10$hashSimulado");

        /*
         * O usuário está ativo.
         */
        when(usuario.getAtivo())
                .thenReturn(true);

        /*
         * -------------------------------------------------
         * 3. REPOSITORY
         * -------------------------------------------------
         */
        when(usuarioRepository
                .findByEmail("paciente@teste.com"))
                .thenReturn(
                        Optional.of(usuario));

        /*
         * Simulamos o BCrypt informando
         * que a senha NÃO corresponde ao hash.
         */
        when(passwordEncoder.matches(
                "senha-incorreta",
                "$2a$10$hashSimulado"))
                .thenReturn(false);

        /*
         * -------------------------------------------------
         * 4. EXECUTA E VERIFICA
         * -------------------------------------------------
         */
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> autenticacaoService
                        .autenticar(dados));

        /*
         * A aplicação utiliza uma mensagem genérica
         * para não revelar se o problema foi
         * e-mail ou senha.
         */
        assertEquals(
                "E-mail ou senha inválidos.",
                excecao.getMessage());
    }

    /*
     * Deve bloquear a autenticação quando
     * o e-mail informado não estiver cadastrado.
     *
     * Por segurança, a aplicação utiliza
     * a mesma mensagem para e-mail inexistente
     * e senha incorreta.
     *
     * Tópicos:
     * - Testes de Software
     * - Segurança
     * - Engenharia de Software
     */
    @Test
    void deveBloquearLoginComEmailInexistente() {

        /*
         * -------------------------------------------------
         * 1. DADOS DE LOGIN
         * -------------------------------------------------
         */
        LoginRequestDTO dados = new LoginRequestDTO();

        dados.setEmail(
                "naoexiste@teste.com");

        dados.setSenha(
                "qualquer-senha");

        /*
         * -------------------------------------------------
         * 2. REPOSITORY
         * -------------------------------------------------
         *
         * Optional.empty() representa a situação
         * em que nenhum usuário foi encontrado
         * com o e-mail informado.
         */
        when(usuarioRepository
                .findByEmail("naoexiste@teste.com"))
                .thenReturn(
                        Optional.empty());

        /*
         * -------------------------------------------------
         * 3. EXECUTA E VERIFICA
         * -------------------------------------------------
         */
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> autenticacaoService
                        .autenticar(dados));

        /*
         * A mensagem continua genérica.
         *
         * Isso evita informar a alguém se
         * determinado e-mail está ou não
         * cadastrado no sistema.
         */
        assertEquals(
                "E-mail ou senha inválidos.",
                excecao.getMessage());
    }

    /*
     * Cenário positivo de autenticação.
     *
     * Deve permitir o login quando:
     *
     * - o e-mail existir;
     * - o usuário estiver ativo;
     * - a senha estiver correta.
     *
     * Nesse caso, a aplicação deve gerar
     * um token JWT e retornar o LoginResponseDTO.
     *
     * Tópicos:
     * - Testes de Software
     * - Segurança
     * - Spring Security
     * - Programação Orientada a Objetos
     * - Arquitetura de Software
     */
    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {

        /*
         * -------------------------------------------------
         * 1. DADOS INFORMADOS NO LOGIN
         * -------------------------------------------------
         */
        LoginRequestDTO dados = new LoginRequestDTO();

        dados.setEmail(
                "paciente@teste.com");

        dados.setSenha(
                "senha-correta");

        /*
         * -------------------------------------------------
         * 2. USUÁRIO SIMULADO
         * -------------------------------------------------
         */
        Usuario usuario = org.mockito.Mockito.mock(
                Usuario.class);

        /*
         * Estes dados serão utilizados pela
         * AutenticacaoService para montar
         * o LoginResponseDTO.
         */
        when(usuario.getIdUsuario())
                .thenReturn(6L);

        when(usuario.getNome())
                .thenReturn(
                        "Paciente Teste");

        when(usuario.getEmail())
                .thenReturn(
                        "paciente@teste.com");

        when(usuario.getPerfil())
                .thenReturn(
                        TipoPerfil.PACIENTE);

        /*
         * O usuário precisa estar ativo
         * para conseguir autenticar.
         */
        when(usuario.getAtivo())
                .thenReturn(true);

        /*
         * O banco armazena o hash da senha,
         * e não a senha em texto puro.
         */
        when(usuario.getSenha())
                .thenReturn(
                        "$2a$10$hashSimulado");

        /*
         * -------------------------------------------------
         * 3. REPOSITORY
         * -------------------------------------------------
         *
         * Simulamos que o e-mail informado
         * foi encontrado no banco.
         */
        when(usuarioRepository
                .findByEmail("paciente@teste.com"))
                .thenReturn(
                        Optional.of(usuario));

        /*
         * -------------------------------------------------
         * 4. VALIDAÇÃO DA SENHA
         * -------------------------------------------------
         *
         * PasswordEncoder.matches() retorna true,
         * representando uma senha correta.
         */
        when(passwordEncoder.matches(
                "senha-correta",
                "$2a$10$hashSimulado"))
                .thenReturn(true);

        /*
         * -------------------------------------------------
         * 5. GERAÇÃO DO JWT
         * -------------------------------------------------
         *
         * Não precisamos gerar um JWT real neste
         * teste unitário.
         *
         * Simulamos o JwtService retornando
         * um token de teste.
         */
        when(jwtService
                .gerarToken(usuario))
                .thenReturn(
                        "jwt-token-teste");

        /*
         * -------------------------------------------------
         * 6. EXECUTA A AUTENTICAÇÃO
         * -------------------------------------------------
         */
        LoginResponseDTO resposta = autenticacaoService
                .autenticar(dados);

        /*
         * -------------------------------------------------
         * 7. VERIFICA O RESULTADO
         * -------------------------------------------------
         *
         * Se chegamos até aqui sem exceção
         * e recebemos uma resposta, o login
         * foi concluído.
         */
        assertNotNull(resposta);

        /*
         * Também confirmamos que a Service
         * realmente solicitou a geração do JWT.
         */
        verify(jwtService)
                .gerarToken(usuario);
    }

    /*
     * Deve bloquear a autenticação quando
     * o usuário estiver inativo.
     *
     * Mesmo que o e-mail exista, usuários
     * desativados não podem acessar o sistema.
     *
     * Tópicos:
     * - Testes de Software
     * - Segurança
     * - Spring Security
     * - Engenharia de Software
     */
    @Test
    void deveBloquearLoginDeUsuarioInativo() {

        /*
         * -------------------------------------------------
         * 1. DADOS DE LOGIN
         * -------------------------------------------------
         */
        LoginRequestDTO dados = new LoginRequestDTO();

        dados.setEmail(
                "usuario.inativo@teste.com");

        dados.setSenha(
                "senha-correta");

        /*
         * -------------------------------------------------
         * 2. USUÁRIO SIMULADO
         * -------------------------------------------------
         */
        Usuario usuario = org.mockito.Mockito.mock(
                Usuario.class);

        /*
         * A principal condição deste teste:
         *
         * ativo = false
         */
        when(usuario.getAtivo())
                .thenReturn(false);

        /*
         * -------------------------------------------------
         * 3. REPOSITORY
         * -------------------------------------------------
         *
         * O usuário existe no sistema.
         */
        when(usuarioRepository
                .findByEmail(
                        "usuario.inativo@teste.com"))
                .thenReturn(
                        Optional.of(usuario));

        /*
         * -------------------------------------------------
         * 4. EXECUTA E VERIFICA
         * -------------------------------------------------
         */
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> autenticacaoService
                        .autenticar(dados));

        /*
         * A mensagem permanece genérica.
         *
         * Isso evita revelar detalhes
         * sobre a conta do usuário.
         */
        assertEquals(
                "E-mail ou senha inválidos.",
                excecao.getMessage());

        /*
         * Como o usuário já está inativo,
         * o fluxo deve parar antes mesmo
         * de verificar a senha.
         */
        verify(
                passwordEncoder,
                org.mockito.Mockito.never()).matches(
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString());

        /*
         * Também não deve gerar JWT.
         */
        verify(
                jwtService,
                org.mockito.Mockito.never()).gerarToken(
                        org.mockito.ArgumentMatchers.any(Usuario.class));
    }

}