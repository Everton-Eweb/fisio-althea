package br.com.althea.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.althea.api.dto.ProfissionalRequestDTO;
import br.com.althea.api.dto.ProfissionalResponseDTO;
import br.com.althea.api.entity.TipoPerfil;
import br.com.althea.api.entity.Profissional;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.repository.ProfissionalRepository;
import br.com.althea.api.repository.UsuarioRepository;

/*
 * Testes unitários do cadastro de profissionais.
 *
 * Os repositories e o PasswordEncoder são
 * simulados com Mockito.
 *
 * Dessa forma, nenhum registro real
 * será criado no banco durante os testes.
 *
 * Tópicos:
 * - Testes de Software
 * - Levantamento e Especificação de Requisitos
 * - Segurança
 * - Spring Data JPA
 * - Arquitetura de Software
 */
@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    /*
     * O Mockito cria a ProfissionalService
     * utilizando as dependências simuladas.
     */
    @InjectMocks
    private ProfissionalService profissionalService;

    /*
     * RF02
     *
     * Não deve permitir cadastrar um profissional
     * quando o e-mail informado já estiver
     * sendo utilizado.
     */
    @Test
    void deveBloquearCadastroComEmailJaExistente() {

        /*
         * -------------------------------------------------
         * 1. DADOS DO PROFISSIONAL
         * -------------------------------------------------
         */
        ProfissionalRequestDTO dados = new ProfissionalRequestDTO();

        dados.setNome(
                "Profissional Teste");

        dados.setEmail(
                "existente@teste.com");

        dados.setSenha(
                "senha-teste");

        /*
         * -------------------------------------------------
         * 2. E-MAIL JÁ EXISTENTE
         * -------------------------------------------------
         *
         * Simulamos que o Repository encontrou
         * outro usuário utilizando o mesmo e-mail.
         */
        when(usuarioRepository
                .existsByEmail(
                        "existente@teste.com"))
                .thenReturn(true);

        /*
         * -------------------------------------------------
         * 3. EXECUTA
         * -------------------------------------------------
         */
        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> profissionalService
                        .cadastrar(dados));

        /*
         * -------------------------------------------------
         * 4. CONFIRMA A REGRA
         * -------------------------------------------------
         */
        assertEquals(
                "Já existe um usuário cadastrado com este e-mail.",
                excecao.getMessage());

        /*
         * -------------------------------------------------
         * 5. GARANTE QUE NADA FOI PERSISTIDO
         * -------------------------------------------------
         *
         * Como a validação falhou logo no início,
         * nenhum Usuario deve ser criado.
         */
        verify(
                usuarioRepository,
                never()).save(
                        org.mockito.ArgumentMatchers
                                .any(Usuario.class));

        /*
         * Consequentemente nenhum Profissional
         * também deve ser criado.
         */
        verify(
                profissionalRepository,
                never()).save(
                        org.mockito.ArgumentMatchers
                                .any(Profissional.class));

        /*
         * A senha também não precisa ser
         * codificada porque o cadastro
         * já foi rejeitado.
         */
        verify(
                passwordEncoder,
                never()).encode(
                        org.mockito.ArgumentMatchers
                                .anyString());
    }

    /*
     * RF02 - cenário positivo.
     *
     * Deve cadastrar um profissional válido garantindo:
     *
     * - e-mail disponível;
     * - senha codificada;
     * - perfil PROFISSIONAL;
     * - usuário ativo;
     * - vínculo entre Usuario e Profissional;
     * - persistência dos dados profissionais.
     *
     * Tópicos:
     * - Testes de Software
     * - Levantamento e Especificação de Requisitos
     * - Segurança
     * - Programação Orientada a Objetos
     * - Spring Data JPA
     */
    @Test
    void deveCadastrarProfissionalComDadosValidos() {

        /*
         * -------------------------------------------------
         * 1. DADOS RECEBIDOS PELA API
         * -------------------------------------------------
         */
        ProfissionalRequestDTO dados = new ProfissionalRequestDTO();

        dados.setNome(
                "Profissional Teste");

        dados.setEmail(
                "novo.profissional@teste.com");

        dados.setSenha(
                "senha-original");

        dados.setCrefito(
                "CREFITO-12345");

        dados.setEspecialidade(
                "Fisioterapia");

        dados.setTelefone(
                "(54) 99999-9999");

        /*
         * -------------------------------------------------
         * 2. E-MAIL DISPONÍVEL
         * -------------------------------------------------
         *
         * false significa que ainda não existe
         * usuário com esse e-mail.
         */
        when(usuarioRepository
                .existsByEmail(
                        "novo.profissional@teste.com"))
                .thenReturn(false);

        /*
         * -------------------------------------------------
         * 3. CODIFICAÇÃO DA SENHA
         * -------------------------------------------------
         *
         * Simulamos o PasswordEncoder.
         *
         * Não precisamos executar BCrypt real
         * neste teste unitário.
         */
        when(passwordEncoder
                .encode("senha-original"))
                .thenReturn(
                        "$2a$10$hashProfissionalTeste");

        /*
         * -------------------------------------------------
         * 4. SALVAMENTO DO USUÁRIO
         * -------------------------------------------------
         *
         * A Service cria um Usuario real.
         *
         * O Repository devolve o mesmo objeto
         * recebido.
         */
        when(usuarioRepository
                .save(
                        org.mockito.ArgumentMatchers
                                .any(Usuario.class)))
                .thenAnswer(
                        invocacao -> invocacao.getArgument(0));

        /*
         * -------------------------------------------------
         * 5. SALVAMENTO DO PROFISSIONAL
         * -------------------------------------------------
         */
        when(profissionalRepository
                .save(
                        org.mockito.ArgumentMatchers
                                .any(Profissional.class)))
                .thenAnswer(
                        invocacao -> invocacao.getArgument(0));

        /*
         * -------------------------------------------------
         * 6. EXECUTA O CADASTRO
         * -------------------------------------------------
         */
        ProfissionalResponseDTO resposta = profissionalService
                .cadastrar(dados);

        /*
         * O cadastro deve terminar normalmente
         * e retornar um DTO.
         */
        assertNotNull(resposta);

        /*
         * -------------------------------------------------
         * 7. CAPTURA O USUÁRIO SALVO
         * -------------------------------------------------
         */
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(
                Usuario.class);

        verify(usuarioRepository)
                .save(
                        usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();

        /*
         * -------------------------------------------------
         * 8. CONFERE O USUÁRIO
         * -------------------------------------------------
         */
        assertEquals(
                "Profissional Teste",
                usuarioSalvo.getNome());

        assertEquals(
                "novo.profissional@teste.com",
                usuarioSalvo.getEmail());

        /*
         * A senha persistida deve ser o hash,
         * e não a senha original.
         */
        assertEquals(
                "$2a$10$hashProfissionalTeste",
                usuarioSalvo.getSenha());

        /*
         * O perfil é definido pelo próprio sistema.
         */
        assertEquals(
                TipoPerfil.PROFISSIONAL,
                usuarioSalvo.getPerfil());

        /*
         * Todo novo profissional começa ativo.
         */
        assertEquals(
                true,
                usuarioSalvo.getAtivo());

        /*
         * -------------------------------------------------
         * 9. CAPTURA O PROFISSIONAL SALVO
         * -------------------------------------------------
         */
        ArgumentCaptor<Profissional> profissionalCaptor = ArgumentCaptor.forClass(
                Profissional.class);

        verify(profissionalRepository)
                .save(
                        profissionalCaptor.capture());

        Profissional profissionalSalvo = profissionalCaptor.getValue();

        /*
         * -------------------------------------------------
         * 10. CONFERE O RELACIONAMENTO
         * -------------------------------------------------
         *
         * O Profissional deve estar relacionado
         * exatamente ao Usuario criado anteriormente.
         */
        assertSame(
                usuarioSalvo,
                profissionalSalvo.getUsuario());

        /*
         * -------------------------------------------------
         * 11. CONFERE OS DADOS PROFISSIONAIS
         * -------------------------------------------------
         */
        assertEquals(
                "CREFITO-12345",
                profissionalSalvo.getCrefito());

        assertEquals(
                "Fisioterapia",
                profissionalSalvo.getEspecialidade());

        assertEquals(
                "(54) 99999-9999",
                profissionalSalvo.getTelefone());
    }
}