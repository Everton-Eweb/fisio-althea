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
import org.springframework.web.bind.annotation.PatchMapping;

import br.com.althea.api.dto.MensagemRequestDTO;
import br.com.althea.api.dto.MensagemResponseDTO;
import br.com.althea.api.service.MensagemService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import br.com.althea.api.entity.TipoPerfil;

/*
 * Controller responsável pelas requisições HTTP
 * relacionadas às mensagens do sistema.
 *
 * Fluxo:
 *
 * Postman / Front-end
 *        ↓
 * MensagemController
 *        ↓
 * MensagemService
 *        ↓
 * Repositories
 *        ↓
 * MySQL
 *
 * Tópicos:
 * - APIs REST
 * - Spring Framework
 * - Spring Boot
 * - Arquitetura de Software
 * - Programação Orientada a Objetos
 */
@RestController
@RequestMapping("/api/mensagens")
public class MensagemController {

    /*
     * Referência para a camada Service.
     *
     * O Controller não acessa diretamente
     * o banco de dados.
     */
    private final MensagemService mensagemService;

    /*
     * Injeção de dependência pelo construtor.
     *
     * O Spring fornece automaticamente
     * uma instância de MensagemService.
     */
    public MensagemController(
            MensagemService mensagemService) {

        this.mensagemService = mensagemService;
    }

    /*
     * GET /api/mensagens
     *
     * Retorna todas as mensagens cadastradas.
     */
    @GetMapping
    public List<MensagemResponseDTO> listarTodas() {

        return mensagemService.listarTodas();
    }

    /*
     * GET /api/mensagens/me
     *
     * Retorna somente as mensagens relacionadas
     * ao paciente vinculado ao usuário autenticado.
     *
     * O usuário não informa idPaciente.
     * O idUsuario é obtido diretamente do JWT.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     * - Programação Orientada a Objetos
     */
    @GetMapping("/me")
    public ResponseEntity<List<MensagemResponseDTO>> listarMinhasMensagens(
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * Recupera o idUsuario presente no JWT.
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * A Service busca somente as mensagens
         * relacionadas ao paciente desse usuário.
         */
        List<MensagemResponseDTO> mensagens = mensagemService.listarMinhasMensagens(
                idUsuario);

        /*
         * Retorna HTTP 200 OK.
         */
        return ResponseEntity.ok(mensagens);
    }

    /*
     * GET /api/mensagens/{id}
     *
     * Consulta uma mensagem específica considerando
     * o usuário e o perfil presentes no JWT.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     */
    @GetMapping("/{id}")
    public ResponseEntity<MensagemResponseDTO> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * Recupera o identificador do usuário
         * autenticado diretamente do token.
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * Recupera o perfil gravado no JWT.
         */
        String perfilClaim = jwt.getClaimAsString("perfil");

        TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

        /*
         * A Service decide se esse usuário
         * possui autorização para consultar
         * a mensagem solicitada.
         */
        MensagemResponseDTO mensagem = mensagemService.buscarPorIdComAutorizacao(
                id,
                idUsuario,
                perfil);

        return ResponseEntity.ok(mensagem);
    }

    /*
     * POST /api/mensagens
     *
     * Cadastra uma mensagem utilizando a identidade
     * presente no token JWT.
     *
     * O cliente não informa mais idUsuario.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     */
    @PostMapping
    public ResponseEntity<MensagemResponseDTO> cadastrar(
            @Valid @RequestBody MensagemRequestDTO dados,
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * Recupera o idUsuario autenticado.
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * Recupera o perfil autenticado.
         *
         * Exemplo:
         * PACIENTE
         * PROFISSIONAL
         * RECEPCIONISTA
         * ADMINISTRADOR
         */
        String perfilClaim = jwt.getClaimAsString("perfil");

        TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

        /*
         * A Service utiliza o usuário do JWT
         * como remetente da mensagem.
         */
        MensagemResponseDTO mensagemCriada = mensagemService.cadastrarComAutorizacao(
                dados,
                idUsuario,
                perfil);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mensagemCriada);
    }

    /*
     * PATCH /api/mensagens/{id}/lida
     *
     * Marca uma mensagem como lida considerando
     * o usuário atualmente autenticado.
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
     * PROFISSIONAL
     * -> não possui acesso nesta operação
     * no modelo atual.
     *
     * Tópicos:
     * - Segurança
     * - Spring Security
     * - APIs REST
     * - Arquitetura de Software
     * - Programação Orientada a Objetos
     */
    @PatchMapping("/{id}/lida")
    public ResponseEntity<MensagemResponseDTO> marcarComoLida(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        /*
         * -------------------------------------------------
         * 1. RECUPERA O ID DO USUÁRIO
         * -------------------------------------------------
         *
         * O idUsuario foi gravado no token
         * durante a autenticação.
         */
        Number idUsuarioClaim = jwt.getClaim("idUsuario");

        Long idUsuario = idUsuarioClaim.longValue();

        /*
         * -------------------------------------------------
         * 2. RECUPERA O PERFIL
         * -------------------------------------------------
         *
         * Exemplo:
         *
         * PACIENTE
         * RECEPCIONISTA
         * ADMINISTRADOR
         */
        String perfilClaim = jwt.getClaimAsString("perfil");

        TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

        /*
         * -------------------------------------------------
         * 3. MARCA COMO LIDA COM AUTORIZAÇÃO
         * -------------------------------------------------
         *
         * A Service verificará primeiro
         * se o usuário possui permissão
         * sobre aquela mensagem.
         */
        MensagemResponseDTO mensagemAtualizada = mensagemService.marcarComoLidaComAutorizacao(
                id,
                idUsuario,
                perfil);

        /*
         * Retorna HTTP 200 OK.
         */
        return ResponseEntity.ok(mensagemAtualizada);
    }
}