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

import br.com.althea.api.dto.ReagendamentoRequestDTO;
import br.com.althea.api.dto.AgendamentoRequestDTO;
import br.com.althea.api.dto.AgendamentoResponseDTO;
import br.com.althea.api.service.AgendamentoService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import br.com.althea.api.entity.TipoPerfil;

/*
 * Controller responsável pelas requisições HTTP
 * relacionadas aos agendamentos da Clínica Althea.
 *
 * O Controller recebe a requisição e encaminha
 * a operação para a camada Service.
 *
 * Fluxo:
 *
 * Postman / Front-end
 *        ↓
 * AgendamentoController
 *        ↓
 * AgendamentoService
 *        ↓
 * Repositories
 *        ↓
 * MySQL
 *
 * Tópicos:
 * - APIs REST
 * - Spring Boot
 * - Spring Framework
 * - Arquitetura de Software
 * - Análise e Projeto de Sistemas
 */
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

        /*
         * Referência para a camada Service.
         *
         * O Controller não acessa diretamente
         * os Repositories ou o banco de dados.
         */
        private final AgendamentoService agendamentoService;

        /*
         * Injeção de dependência pelo construtor.
         *
         * O Spring fornece automaticamente uma
         * instância de AgendamentoService.
         */
        public AgendamentoController(
                        AgendamentoService agendamentoService) {

                this.agendamentoService = agendamentoService;
        }

        /*
         * GET /api/agendamentos
         *
         * Retorna todos os agendamentos cadastrados.
         */
        @GetMapping
        public List<AgendamentoResponseDTO> listarTodos() {

                return agendamentoService.listarTodos();
        }

        /*
         * GET /api/agendamentos/me
         *
         * Retorna somente os agendamentos relacionados
         * ao usuário atualmente autenticado.
         *
         * O comportamento depende do perfil:
         *
         * PACIENTE
         * -> próprios agendamentos.
         *
         * PROFISSIONAL
         * -> atendimentos atribuídos ao profissional.
         *
         * Tópicos:
         * - Segurança
         * - Spring Security
         * - APIs REST
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @GetMapping("/me")
        public ResponseEntity<List<AgendamentoResponseDTO>> listarMeusAgendamentos(
                        @AuthenticationPrincipal Jwt jwt) {

                /*
                 * -------------------------------------------------
                 * 1. RECUPERA O ID DO USUÁRIO
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
                 * 2. RECUPERA O PERFIL
                 * -------------------------------------------------
                 *
                 * Exemplo:
                 *
                 * "perfil": "PACIENTE"
                 */
                String perfilClaim = jwt.getClaimAsString("perfil");

                /*
                 * Converte a String para o enum
                 * TipoPerfil utilizado no projeto.
                 */
                TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

                /*
                 * -------------------------------------------------
                 * 3. BUSCA OS AGENDAMENTOS
                 * -------------------------------------------------
                 *
                 * A Service decide automaticamente
                 * qual consulta deverá ser realizada
                 * de acordo com o perfil.
                 */
                List<AgendamentoResponseDTO> agendamentos = agendamentoService.listarMeusAgendamentos(
                                idUsuario,
                                perfil);

                /*
                 * Retorna HTTP 200 OK.
                 */
                return ResponseEntity.ok(agendamentos);
        }

        /*
         * GET /api/agendamentos/{id}
         *
         * Busca um agendamento específico pelo ID.
         *
         * Exemplo:
         * GET /api/agendamentos/1
         */
        @GetMapping("/{id}")
        public ResponseEntity<AgendamentoResponseDTO> buscarPorId(
                        @PathVariable Long id) {

                /*
                 * Se o agendamento existir:
                 * retorna HTTP 200 OK.
                 *
                 * Se não existir:
                 * retorna HTTP 404 Not Found.
                 */
                return agendamentoService.buscarPorId(id)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /*
         * POST /api/agendamentos
         *
         * Cria um novo agendamento levando em consideração
         * o usuário atualmente autenticado.
         *
         * O JWT fornece:
         *
         * - idUsuario;
         * - perfil.
         *
         * Esses dados são enviados para a Service,
         * que realizará a autorização antes das
         * regras de negócio do agendamento.
         *
         * Tópicos:
         * - Segurança
         * - Spring Security
         * - APIs REST
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @PostMapping
        public ResponseEntity<AgendamentoResponseDTO> cadastrar(
                        @Valid @RequestBody AgendamentoRequestDTO dados,
                        @AuthenticationPrincipal Jwt jwt) {

                /*
                 * -------------------------------------------------
                 * 1. RECUPERA O ID DO USUÁRIO
                 * -------------------------------------------------
                 *
                 * O JwtService adicionou ao token:
                 *
                 * "idUsuario": ...
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
                 * "perfil": "PACIENTE"
                 */
                String perfilClaim = jwt.getClaimAsString("perfil");

                TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

                /*
                 * -------------------------------------------------
                 * 3. CRIA O AGENDAMENTO COM AUTORIZAÇÃO
                 * -------------------------------------------------
                 *
                 * A Service verificará primeiro:
                 *
                 * PACIENTE
                 * -> só pode agendar para si próprio.
                 *
                 * PROFISSIONAL
                 * -> não pode criar agendamento.
                 *
                 * RECEPCIONISTA / ADMINISTRADOR
                 * -> podem criar para outros pacientes.
                 *
                 * Depois disso continuam sendo verificadas
                 * RN01, RN02 e as demais regras já existentes.
                 */
                AgendamentoResponseDTO agendamentoCriado = agendamentoService.cadastrarComAutorizacao(
                                dados,
                                idUsuario,
                                perfil);

                /*
                 * Retorna HTTP 201 Created.
                 */
                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(agendamentoCriado);
        }

        /*
         * PATCH /api/agendamentos/{id}/confirmar
         *
         * Confirma um agendamento considerando
         * o usuário atualmente autenticado.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode confirmar.
         *
         * RECEPCIONISTA
         * -> pode confirmar.
         *
         * PROFISSIONAL
         * -> somente os próprios agendamentos.
         *
         * PACIENTE
         * -> não pode confirmar.
         *
         * Tópicos:
         * - Segurança
         * - Spring Security
         * - APIs REST
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @PatchMapping("/{id}/confirmar")
        public ResponseEntity<AgendamentoResponseDTO> confirmar(
                        @PathVariable Long id,
                        @AuthenticationPrincipal Jwt jwt) {

                /*
                 * Recupera o idUsuario presente no JWT.
                 */
                Number idUsuarioClaim = jwt.getClaim("idUsuario");

                Long idUsuario = idUsuarioClaim.longValue();

                /*
                 * Recupera o perfil presente no JWT.
                 *
                 * Exemplo:
                 * PROFISSIONAL
                 */
                String perfilClaim = jwt.getClaimAsString("perfil");

                TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

                /*
                 * A Service fará a autorização antes
                 * de confirmar o agendamento.
                 */
                AgendamentoResponseDTO agendamentoConfirmado = agendamentoService.confirmarComAutorizacao(
                                id,
                                idUsuario,
                                perfil);

                /*
                 * Retorna o registro atualizado.
                 */
                return ResponseEntity.ok(agendamentoConfirmado);
        }

        /*
         * PATCH /api/agendamentos/{id}/cancelar
         *
         * Cancela um agendamento considerando
         * o usuário atualmente autenticado.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode cancelar qualquer agendamento.
         *
         * RECEPCIONISTA
         * -> pode cancelar qualquer agendamento.
         *
         * PACIENTE
         * -> somente os próprios agendamentos.
         *
         * PROFISSIONAL
         * -> somente os agendamentos atribuídos a ele.
         *
         * Tópicos:
         * - Segurança
         * - Spring Security
         * - APIs REST
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @PatchMapping("/{id}/cancelar")
        public ResponseEntity<AgendamentoResponseDTO> cancelar(
                        @PathVariable Long id,
                        @AuthenticationPrincipal Jwt jwt) {

                /*
                 * -------------------------------------------------
                 * 1. RECUPERA O ID DO USUÁRIO
                 * -------------------------------------------------
                 */
                Number idUsuarioClaim = jwt.getClaim("idUsuario");

                Long idUsuario = idUsuarioClaim.longValue();

                /*
                 * -------------------------------------------------
                 * 2. RECUPERA O PERFIL
                 * -------------------------------------------------
                 */
                String perfilClaim = jwt.getClaimAsString("perfil");

                TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

                /*
                 * -------------------------------------------------
                 * 3. CANCELA COM AUTORIZAÇÃO
                 * -------------------------------------------------
                 *
                 * A Service verificará se PACIENTE
                 * ou PROFISSIONAL possuem relação
                 * com o agendamento solicitado.
                 */
                AgendamentoResponseDTO agendamentoCancelado = agendamentoService.cancelarComAutorizacao(
                                id,
                                idUsuario,
                                perfil);

                /*
                 * Retorna o registro atualizado.
                 */
                return ResponseEntity.ok(agendamentoCancelado);
        }

        /*
         * PATCH /api/agendamentos/{id}/reagendar
         *
         * Reagenda um atendimento considerando
         * o usuário atualmente autenticado.
         *
         * Regras:
         *
         * ADMINISTRADOR
         * -> pode reagendar qualquer agendamento.
         *
         * RECEPCIONISTA
         * -> pode reagendar qualquer agendamento.
         *
         * PACIENTE
         * -> somente os próprios agendamentos.
         *
         * PROFISSIONAL
         * -> somente os atendimentos atribuídos a ele.
         *
         * Depois da autorização, a Service continua
         * aplicando as regras da RN04:
         *
         * - disponibilidade;
         * - conflito de horário.
         *
         * Tópicos:
         * - Segurança
         * - Spring Security
         * - APIs REST
         * - Arquitetura de Software
         * - Programação Orientada a Objetos
         */
        @PatchMapping("/{id}/reagendar")
        public ResponseEntity<AgendamentoResponseDTO> reagendar(
                        @PathVariable Long id,
                        @Valid @RequestBody ReagendamentoRequestDTO dados,
                        @AuthenticationPrincipal Jwt jwt) {

                /*
                 * -------------------------------------------------
                 * 1. RECUPERA O ID DO USUÁRIO
                 * -------------------------------------------------
                 */
                Number idUsuarioClaim = jwt.getClaim("idUsuario");

                Long idUsuario = idUsuarioClaim.longValue();

                /*
                 * -------------------------------------------------
                 * 2. RECUPERA O PERFIL
                 * -------------------------------------------------
                 */
                String perfilClaim = jwt.getClaimAsString("perfil");

                TipoPerfil perfil = TipoPerfil.valueOf(perfilClaim);

                /*
                 * -------------------------------------------------
                 * 3. REAGENDA COM AUTORIZAÇÃO
                 * -------------------------------------------------
                 *
                 * A Service primeiro verifica se o usuário
                 * possui relação com o agendamento.
                 *
                 * Depois aplica disponibilidade e conflito.
                 */
                AgendamentoResponseDTO agendamentoReagendado = agendamentoService.reagendarComAutorizacao(
                                id,
                                dados,
                                idUsuario,
                                perfil);

                /*
                 * Retorna HTTP 200 OK.
                 */
                return ResponseEntity.ok(agendamentoReagendado);
        }
}