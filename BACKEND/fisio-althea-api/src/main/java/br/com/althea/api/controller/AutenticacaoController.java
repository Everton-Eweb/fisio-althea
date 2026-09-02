package br.com.althea.api.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.althea.api.dto.LoginRequestDTO;
import br.com.althea.api.dto.LoginResponseDTO;

import br.com.althea.api.service.AutenticacaoService;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

/*
 * Controller responsável pelas requisições
 * relacionadas à autenticação.
 *
 * Fluxo:
 *
 * Front-end / Postman
 *        ↓
 * AutenticacaoController
 *        ↓
 * AutenticacaoService
 *        ↓
 * UsuarioRepository
 *        ↓
 * MySQL
 *
 * Tópicos:
 * - APIs REST
 * - Spring Framework
 * - Spring Boot
 * - Arquitetura de Software
 * - Segurança
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    /*
     * Referência para a camada de serviço
     * responsável pela autenticação.
     */
    private final AutenticacaoService autenticacaoService;


    /*
     * Injeção de dependência pelo construtor.
     */
    public AutenticacaoController(
            AutenticacaoService autenticacaoService) {

        this.autenticacaoService = autenticacaoService;
    }

    /*
     * POST /api/auth/login
     *
     * Recebe:
     *
     * - e-mail;
     * - senha.
     *
     * Se as credenciais estiverem corretas,
     * retorna os dados básicos do usuário.
     */
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dados) {

        /*
         * A camada Service:
         *
         * - localiza o usuário;
         * - verifica se está ativo;
         * - compara a senha utilizando BCrypt.
         */
        LoginResponseDTO usuarioAutenticado =
                autenticacaoService.autenticar(dados);


        /*
         * Retorna HTTP 200 OK.
         */
        return ResponseEntity.ok(usuarioAutenticado);
    }
}