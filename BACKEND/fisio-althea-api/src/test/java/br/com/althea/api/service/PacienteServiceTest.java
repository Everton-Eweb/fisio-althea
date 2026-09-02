package br.com.althea.api.service;

import java.time.LocalDate;

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

import br.com.althea.api.dto.PacienteRequestDTO;
import br.com.althea.api.entity.Paciente;
import br.com.althea.api.entity.Usuario;
import br.com.althea.api.entity.TipoPerfil;

import br.com.althea.api.repository.PacienteRepository;
import br.com.althea.api.repository.UsuarioRepository;

import br.com.althea.api.dto.PacienteResponseDTO;

/*
 * Testes unitários do cadastro de pacientes.
 *
 * Os repositories e o PasswordEncoder são
 * simulados com Mockito.
 *
 * Portanto nenhum registro real será criado
 * no banco MySQL durante estes testes.
 *
 * Tópicos:
 * - Testes de Software
 * - Levantamento e Especificação de Requisitos
 * - Segurança
 * - Spring Data JPA
 * - Arquitetura de Software
 */
@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    /*
     * O Mockito cria a PacienteService
     * utilizando os mocks acima.
     */
    @InjectMocks
    private PacienteService pacienteService;

    /*
     * RF01
     *
     * Não deve permitir o cadastro de um novo
     * paciente quando o e-mail informado já
     * estiver sendo utilizado por outro usuário.
     */
    @Test
    void deveBloquearCadastroComEmailJaExistente() {

        /*
         * -------------------------------------------------
         * 1. DADOS DO NOVO PACIENTE
         * -------------------------------------------------
         */
        PacienteRequestDTO dados = new PacienteRequestDTO();

        dados.setNome(
                "Paciente Teste");

        dados.setEmail(
                "existente@teste.com");

        dados.setSenha(
                "senha-teste");

        /*
         * -------------------------------------------------
         * 2. SIMULA O E-MAIL JÁ CADASTRADO
         * -------------------------------------------------
         *
         * O método cadastrar() consulta
         * existsByEmail() antes de criar
         * qualquer entidade.
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
                () -> pacienteService
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
         * 5. GARANTE QUE NADA FOI SALVO
         * -------------------------------------------------
         *
         * Como o e-mail já estava em uso,
         * o fluxo deve parar antes da criação
         * do Usuario e do Paciente.
         */
        verify(
                usuarioRepository,
                never()).save(
                        org.mockito.ArgumentMatchers
                                .any(Usuario.class));

        verify(
                pacienteRepository,
                never()).save(
                        org.mockito.ArgumentMatchers
                                .any(Paciente.class));

        /*
         * Também não há motivo para gerar
         * hash da senha quando o cadastro
         * já foi rejeitado pelo e-mail.
         */
        verify(
                passwordEncoder,
                never()).encode(
                        org.mockito.ArgumentMatchers
                                .anyString());
    }

    /*
     * RF01 - cenário positivo.
     *
     * Deve cadastrar um paciente válido garantindo que:
     *
     * - o e-mail ainda não esteja sendo utilizado;
     * - a senha seja codificada;
     * - o perfil seja definido como PACIENTE;
     * - o usuário seja criado como ativo;
     * - o Paciente fique relacionado ao Usuario criado;
     * - nenhuma senha seja exposta no DTO.
     *
     * Tópicos:
     * - Testes de Software
     * - Levantamento e Especificação de Requisitos
     * - Segurança
     * - Programação Orientada a Objetos
     * - Spring Data JPA
     */
    @Test
    void deveCadastrarPacienteComDadosValidos() {

        /*
         * -------------------------------------------------
         * 1. DADOS RECEBIDOS PELA API
         * -------------------------------------------------
         */
        PacienteRequestDTO dados = new PacienteRequestDTO();

        dados.setNome(
                "Paciente Teste");

        dados.setEmail(
                "novo.paciente@teste.com");

        dados.setSenha(
                "senha-original");

        dados.setCpf(
                "123.456.789-00");

        dados.setDataNascimento(
                LocalDate.of(
                        1990,
                        5,
                        15));

        dados.setTelefone(
                "(54) 99999-9999");

        dados.setEndereco(
                "Rua de Teste, 100");

        /*
         * -------------------------------------------------
         * 2. E-MAIL DISPONÍVEL
         * -------------------------------------------------
         *
         * false significa que ainda não existe
         * outro usuário utilizando esse e-mail.
         */
        when(usuarioRepository
                .existsByEmail(
                        "novo.paciente@teste.com"))
                .thenReturn(false);

        /*
         * -------------------------------------------------
         * 3. CODIFICAÇÃO DA SENHA
         * -------------------------------------------------
         *
         * Não precisamos executar BCrypt real
         * neste teste unitário.
         *
         * Simulamos o PasswordEncoder retornando
         * um hash.
         */
        when(passwordEncoder
                .encode("senha-original"))
                .thenReturn(
                        "$2a$10$hashGeradoNoTeste");

        /*
         * -------------------------------------------------
         * 4. SALVAMENTO DO USUÁRIO
         * -------------------------------------------------
         *
         * A Service cria um Usuario real.
         *
         * O Repository simulado devolve exatamente
         * o objeto que recebeu.
         */
        when(usuarioRepository
                .save(
                        org.mockito.ArgumentMatchers
                                .any(Usuario.class)))
                .thenAnswer(
                        invocacao -> invocacao.getArgument(0));

        /*
         * -------------------------------------------------
         * 5. SALVAMENTO DO PACIENTE
         * -------------------------------------------------
         *
         * Fazemos o mesmo com o Paciente.
         */
        when(pacienteRepository
                .save(
                        org.mockito.ArgumentMatchers
                                .any(Paciente.class)))
                .thenAnswer(
                        invocacao -> invocacao.getArgument(0));

        /*
         * -------------------------------------------------
         * 6. EXECUTA O CADASTRO
         * -------------------------------------------------
         */
        PacienteResponseDTO resposta = pacienteService
                .cadastrar(dados);

        /*
         * O método deve concluir normalmente
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
         * 8. CONFERE OS DADOS DO USUÁRIO
         * -------------------------------------------------
         */
        assertEquals(
                "Paciente Teste",
                usuarioSalvo.getNome());

        assertEquals(
                "novo.paciente@teste.com",
                usuarioSalvo.getEmail());

        /*
         * A senha armazenada deve ser o hash,
         * e não "senha-original".
         */
        assertEquals(
                "$2a$10$hashGeradoNoTeste",
                usuarioSalvo.getSenha());

        /*
         * O próprio sistema define o perfil.
         * O cliente não escolhe esse valor.
         */
        assertEquals(
                TipoPerfil.PACIENTE,
                usuarioSalvo.getPerfil());

        /*
         * Todo novo usuário começa ativo.
         */
        assertEquals(
                true,
                usuarioSalvo.getAtivo());

        /*
         * -------------------------------------------------
         * 9. CAPTURA O PACIENTE SALVO
         * -------------------------------------------------
         */
        ArgumentCaptor<Paciente> pacienteCaptor = ArgumentCaptor.forClass(
                Paciente.class);

        verify(pacienteRepository)
                .save(
                        pacienteCaptor.capture());

        Paciente pacienteSalvo = pacienteCaptor.getValue();

        /*
         * -------------------------------------------------
         * 10. CONFERE O RELACIONAMENTO
         * -------------------------------------------------
         *
         * O Paciente deve estar relacionado
         * exatamente ao Usuario criado anteriormente.
         */
        assertSame(
                usuarioSalvo,
                pacienteSalvo.getUsuario());

        /*
         * -------------------------------------------------
         * 11. CONFERE OS DADOS DO PACIENTE
         * -------------------------------------------------
         */
        assertEquals(
                "123.456.789-00",
                pacienteSalvo.getCpf());

        assertEquals(
                LocalDate.of(
                        1990,
                        5,
                        15),
                pacienteSalvo.getDataNascimento());

        assertEquals(
                "(54) 99999-9999",
                pacienteSalvo.getTelefone());

        assertEquals(
                "Rua de Teste, 100",
                pacienteSalvo.getEndereco());
    }
}