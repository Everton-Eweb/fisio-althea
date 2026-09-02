package br.com.althea.api.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import br.com.althea.api.dto.ProfissionalRequestDTO;
import br.com.althea.api.dto.ProfissionalResponseDTO;
import br.com.althea.api.service.ProfissionalService;

/*
 * Controller responsável pelas requisições HTTP
 * relacionadas aos profissionais da Clínica Althea.
 *
 * Fluxo:
 *
 * Postman / Front-end
 *        ↓
 * ProfissionalController
 *        ↓
 * ProfissionalService
 *        ↓
 * Repository
 *        ↓
 * MySQL
 *
 * Tópicos:
 * - APIs REST
 * - Spring Boot
 * - Spring Framework
 * - Arquitetura de Software
 * - Desenvolvimento Web
 */
@RestController
@RequestMapping("/api/profissionais")
public class ProfissionalController {

    /*
     * Referência para a camada Service.
     *
     * O Controller não acessa diretamente
     * o banco ou os Repositories.
     */
    private final ProfissionalService profissionalService;

    /*
     * Injeção de dependência pelo construtor.
     *
     * O Spring fornece automaticamente
     * a instância de ProfissionalService.
     */
    public ProfissionalController(
            ProfissionalService profissionalService) {

        this.profissionalService = profissionalService;
    }

    /*
     * GET /api/profissionais
     *
     * Retorna todos os profissionais cadastrados.
     *
     * Utilizamos ProfissionalResponseDTO para evitar
     * exposição de informações sensíveis do Usuario.
     */
    @GetMapping
    public List<ProfissionalResponseDTO> listarTodos() {

        return profissionalService.listarTodos();
    }

    /*
     * GET /api/profissionais/me
     *
     * Retorna os dados do profissional relacionado
     * ao usuário atualmente autenticado.
     *
     * O ID não é informado pela URL.
     * Ele é obtido diretamente do JWT.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     * - Programação Orientada a Objetos
     */
    @GetMapping("/me")
    public ResponseEntity<ProfissionalResponseDTO> buscarMeusDados(
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * Recupera do token o claim:
         *
         * "idUsuario": ...
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        /*
         * Converte o valor para Long,
         * que é o tipo utilizado pelos IDs
         * das entidades do projeto.
         */
        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * A Service localiza o Profissional
         * relacionado ao usuário autenticado.
         */
        ProfissionalResponseDTO profissional = profissionalService.buscarPorUsuario(idUsuario);

        /*
         * Retorna somente os dados
         * daquele profissional.
         */
        return ResponseEntity.ok(profissional);
    }

    /*
     * GET /api/profissionais/{id}
     *
     * Busca um profissional específico pelo ID.
     *
     * Exemplo:
     * GET /api/profissionais/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalResponseDTO> buscarPorId(
            @PathVariable Long id) {

        /*
         * Se o profissional existir:
         * retorna HTTP 200 OK.
         *
         * Se não existir:
         * retorna HTTP 404 Not Found.
         */
        return profissionalService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * POST /api/profissionais
     *
     * Cadastra um novo profissional.
     *
     * @RequestBody transforma o JSON recebido
     * em ProfissionalRequestDTO.
     *
     * @Valid executa as validações definidas
     * no DTO antes de continuar.
     */
    @PostMapping
    public ResponseEntity<ProfissionalResponseDTO> cadastrar(
            @Valid @RequestBody ProfissionalRequestDTO dados) {

        /*
         * A camada Service é responsável por:
         *
         * - criar o Usuario;
         * - gerar o hash da senha;
         * - definir o perfil PROFISSIONAL;
         * - criar o Profissional;
         * - relacionar os dois registros.
         */
        ProfissionalResponseDTO profissionalCriado = profissionalService.cadastrar(dados);

        /*
         * HTTP 201 Created indica que um novo
         * recurso foi criado com sucesso.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(profissionalCriado);
    }
}