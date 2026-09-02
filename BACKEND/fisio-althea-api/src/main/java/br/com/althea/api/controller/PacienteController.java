package br.com.althea.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.althea.api.dto.PacienteRequestDTO;

import br.com.althea.api.dto.PacienteResponseDTO;
import br.com.althea.api.service.PacienteService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/*
 * Controller responsável pelas requisições HTTP
 * relacionadas aos pacientes da Clínica Althea.
 *
 * O Controller recebe a requisição do cliente
 * e encaminha a operação para a camada Service.
 *
 * Fluxo:
 *
 * Navegador / Postman / Front-end
 *              ↓
 *      PacienteController
 *              ↓
 *        PacienteService
 *              ↓
 *      PacienteRepository
 *              ↓
 *             MySQL
 *
 * Tópicos:
 * - APIs REST
 * - Spring Boot
 * - Spring Framework
 * - Arquitetura de Software
 * - Desenvolvimento Web
 */
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    /*
     * Referência para a camada Service.
     *
     * O Controller não acessa o Repository
     * ou o banco de dados diretamente.
     */
    private final PacienteService pacienteService;

    /*
     * Injeção de dependência pelo construtor.
     *
     * O Spring identifica PacienteService através
     * da anotação @Service e fornece automaticamente
     * uma instância para este Controller.
     */
    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    /*
     * GET /api/pacientes
     *
     * Retorna todos os pacientes cadastrados.
     *
     * O retorno utiliza PacienteResponseDTO,
     * evitando expor informações desnecessárias
     * da entidade Usuario, como a senha.
     */
    @GetMapping
    public List<PacienteResponseDTO> listarTodos() {

        return pacienteService.listarTodos();
    }

    /*
     * GET /api/pacientes/me
     *
     * Retorna os dados do paciente relacionado
     * ao usuário atualmente autenticado.
     *
     * O cliente não informa nenhum ID.
     *
     * O idUsuario é recuperado diretamente
     * do token JWT validado pelo Spring Security.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     * - Programação Orientada a Objetos
     */
    @GetMapping("/me")
    public ResponseEntity<PacienteResponseDTO> buscarMeusDados(
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * O JwtService adicionou ao token o claim:
         *
         * "idUsuario": 6
         *
         * Aqui recuperamos esse valor diretamente
         * do JWT autenticado.
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        /*
         * Convertemos Number para Long porque
         * nossas entidades utilizam Long como ID.
         */
        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * A camada Service procura o Paciente
         * relacionado ao usuário autenticado.
         */
        PacienteResponseDTO paciente = pacienteService.buscarPorUsuario(idUsuario);

        /*
         * Retorna somente os dados daquele paciente.
         */
        return ResponseEntity.ok(paciente);
    }

    /*
     * GET /api/pacientes/{id}
     *
     * Busca um paciente específico pelo seu ID.
     *
     * Exemplo:
     * GET /api/pacientes/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteResponseDTO> buscarPorId(
            @PathVariable Long id) {

        /*
         * Se o paciente existir:
         * retorna HTTP 200 OK juntamente com os dados.
         *
         * Se não existir:
         * retorna HTTP 404 Not Found.
         */
        return pacienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * POST /api/pacientes
     *
     * Cadastra um novo paciente.
     *
     * O JSON recebido é convertido automaticamente
     * pelo Spring para PacienteRequestDTO.
     *
     * Tópicos:
     * - APIs REST
     * - Spring Framework
     * - Spring Boot
     * - Arquitetura de Software
     * - Validação de Dados
     */
    @SecurityRequirements
    @PostMapping
    public ResponseEntity<PacienteResponseDTO> cadastrar(
            @Valid @RequestBody PacienteRequestDTO dados) {

        /*
         * @Valid faz o Spring verificar as validações
         * definidas no PacienteRequestDTO antes de
         * executar o cadastro.
         *
         * Exemplo:
         * - nome obrigatório;
         * - e-mail obrigatório e válido;
         * - senha com no mínimo 6 caracteres.
         */

        PacienteResponseDTO pacienteCriado = pacienteService.cadastrar(dados);

        /*
         * HTTP 201 Created indica que um novo
         * recurso foi criado com sucesso.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pacienteCriado);
    }
}