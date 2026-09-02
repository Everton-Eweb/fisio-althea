package br.com.althea.api.controller;

import java.util.List;

import java.time.LocalDate;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import br.com.althea.api.entity.TipoPerfil;

import br.com.althea.api.dto.DisponibilidadeRequestDTO;
import br.com.althea.api.dto.DisponibilidadeResponseDTO;
import br.com.althea.api.dto.HorariosDisponiveisResponseDTO;

import br.com.althea.api.service.DisponibilidadeService;

/*
 * Controller responsável pelas requisições HTTP
 * relacionadas às disponibilidades dos profissionais.
 *
 * Fluxo:
 *
 * Postman / Front-end
 *         ↓
 * DisponibilidadeController
 *         ↓
 * DisponibilidadeService
 *         ↓
 * Repository
 *         ↓
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
@RequestMapping("/api/disponibilidades")
public class DisponibilidadeController {

    /*
     * Referência para a camada Service.
     *
     * O Controller não acessa o banco
     * de dados diretamente.
     */
    private final DisponibilidadeService disponibilidadeService;

    /*
     * Injeção de dependência pelo construtor.
     *
     * O Spring fornece automaticamente
     * uma instância de DisponibilidadeService.
     */
    public DisponibilidadeController(
            DisponibilidadeService disponibilidadeService) {

        this.disponibilidadeService = disponibilidadeService;
    }

    /*
     * GET /api/disponibilidades
     *
     * Retorna todas as disponibilidades cadastradas.
     */
    @GetMapping
    public List<DisponibilidadeResponseDTO> listarTodas() {

        return disponibilidadeService.listarTodas();
    }

    /*
     * GET /api/disponibilidades/{id}
     *
     * Busca uma disponibilidade específica pelo ID.
     *
     * Exemplo:
     * GET /api/disponibilidades/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<DisponibilidadeResponseDTO> buscarPorId(
            @PathVariable Long id) {

        /*
         * Se existir:
         * retorna HTTP 200 OK.
         *
         * Se não existir:
         * retorna HTTP 404 Not Found.
         */
        return disponibilidadeService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * GET /api/disponibilidades/horarios
     *
     * Retorna os horários realmente disponíveis
     * de um profissional para uma determinada
     * data e serviço.
     *
     * Exemplo:
     *
     * /api/disponibilidades/horarios
     * ?idProfissional=3
     * &idServico=1
     * &data=2026-09-02
     *
     * Tópicos:
     * - Levantamento e Especificação de Requisitos
     * - APIs REST
     * - Spring Boot
     * - Arquitetura de Software
     * - Desenvolvimento Web
     */
    @GetMapping("/horarios")
    public ResponseEntity<HorariosDisponiveisResponseDTO> buscarHorariosDisponiveis(

            @RequestParam Long idProfissional,

            @RequestParam Long idServico,

            /*
             * Define explicitamente o formato ISO:
             *
             * AAAA-MM-DD
             *
             * Exemplo:
             * 2026-09-02
             */
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        /*
         * Toda a regra de negócio permanece
         * dentro da DisponibilidadeService.
         *
         * O Controller apenas recebe os parâmetros
         * da requisição e devolve a resposta.
         */
        HorariosDisponiveisResponseDTO resposta = disponibilidadeService
                .buscarHorariosDisponiveis(
                        idProfissional,
                        idServico,
                        data);

        /*
         * HTTP 200 OK
         */
        return ResponseEntity.ok(resposta);
    }

    /*
     * POST /api/disponibilidades
     *
     * Cadastra uma disponibilidade levando em
     * consideração o usuário autenticado.
     *
     * O JWT informa:
     *
     * - idUsuario;
     * - perfil.
     *
     * Esses dados serão utilizados pela Service
     * para verificar se um PROFISSIONAL está
     * cadastrando disponibilidade para ele próprio.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     * - Programação Orientada a Objetos
     */
    @PostMapping
    public ResponseEntity<DisponibilidadeResponseDTO> cadastrar(
            @Valid @RequestBody DisponibilidadeRequestDTO dados,
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * -------------------------------------------------
         * 1. RECUPERA O ID DO USUÁRIO DO JWT
         * -------------------------------------------------
         *
         * O JwtService colocou no token:
         *
         * "idUsuario": ...
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * -------------------------------------------------
         * 2. RECUPERA O PERFIL DO JWT
         * -------------------------------------------------
         *
         * Exemplo:
         *
         * "perfil": "PROFISSIONAL"
         */
        String perfilClaim = jwt.getClaimAsString("perfil");

        /*
         * Converte a String do JWT para
         * o enum TipoPerfil utilizado pelo projeto.
         */
        TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

        /*
         * -------------------------------------------------
         * 3. ENCAMINHA PARA A SERVICE
         * -------------------------------------------------
         *
         * A Service verificará:
         *
         * ADMINISTRADOR
         * → pode cadastrar para qualquer profissional.
         *
         * RECEPCIONISTA
         * → pode cadastrar para qualquer profissional.
         *
         * PROFISSIONAL
         * → somente para ele próprio.
         */
        DisponibilidadeResponseDTO disponibilidadeCriada = disponibilidadeService.cadastrarComAutorizacao(
                dados,
                idUsuario,
                perfil);

        /*
         * Retorna HTTP 201 Created.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(disponibilidadeCriada);
    }
}