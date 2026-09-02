package br.com.althea.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

import br.com.althea.api.entity.Servico;
import br.com.althea.api.service.ServicoService;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * Controller responsável pelas requisições HTTP
 * relacionadas aos serviços da Clínica Althea.
 *
 * Ele representa a camada de entrada da API.
 *
 * Fluxo:
 *
 * Navegador / Front-end
 *         ↓
 * ServicoController
 *         ↓
 * ServicoService
 *         ↓
 * ServicoRepository
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
@RequestMapping("/api/servicos")
public class ServicoController {

    /*
     * Referência para a camada Service.
     *
     * O Controller não acessa o banco diretamente.
     * Ele solicita as operações para ServicoService.
     */
    private final ServicoService servicoService;

    /*
     * Injeção de dependência pelo construtor.
     *
     * O Spring identifica ServicoService através
     * da anotação @Service e fornece automaticamente
     * uma instância para este Controller.
     */
    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    /*
     * Endpoint responsável por listar
     * todos os serviços cadastrados.
     *
     * Método HTTP:
     * GET
     *
     * Endereço:
     * /api/servicos
     *
     * Exemplo:
     * http://localhost:8080/api/servicos
     */
    @GetMapping
    public List<Servico> listarTodos() {

        // Solicita à camada Service todos os serviços
        return servicoService.listarTodos();
    }

    /*
     * Endpoint responsável por cadastrar
     * um novo serviço.
     *
     * Método HTTP:
     * POST
     *
     * Endereço:
     * /api/servicos
     *
     * O @RequestBody informa ao Spring que os dados
     * enviados em JSON devem ser convertidos para
     * um objeto da classe Servico.
     */

    /*
     * GET /api/servicos/{id}
     *
     * Busca um serviço específico pelo seu identificador.
     *
     * Exemplo:
     * GET http://localhost:8080/api/servicos/2
     *
     * O valor informado na URL será recebido pelo parâmetro "id".
     */
    @GetMapping("/{id}")
    public ResponseEntity<Servico> buscarPorId(@PathVariable Long id) {

        /*
         * A camada Service realiza a busca.
         *
         * buscarPorId() retorna Optional<Servico>,
         * porque o registro pode existir ou não.
         */
        return servicoService.buscarPorId(id)

                /*
                 * Caso o serviço exista,
                 * retorna HTTP 200 OK juntamente com o objeto.
                 */
                .map(ResponseEntity::ok)

                /*
                 * Caso não exista,
                 * retorna HTTP 404 Not Found.
                 */
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * POST /api/servicos
     *
     * Cadastra um novo serviço.
     *
     * Como um novo recurso é criado,
     * retornamos HTTP 201 Created.
     *
     * Tópicos:
     * - APIs REST
     * - Spring Framework
     * - Engenharia de Software
     */
    @PostMapping
    public ResponseEntity<Servico> cadastrar(
            @RequestBody Servico servico) {

        /*
         * Salva o serviço através
         * da camada Service.
         */
        Servico servicoCriado = servicoService.salvar(servico);

        /*
         * 201 Created indica corretamente
         * que um novo recurso foi criado.
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(servicoCriado);
    }

    /*
     * PUT /api/servicos/{id}
     *
     * Atualiza um serviço existente.
     *
     * Exemplo:
     * PUT http://localhost:8080/api/servicos/2
     *
     * O ID vem pela URL e os novos dados
     * são recebidos através do corpo JSON.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Servico> atualizar(
            @PathVariable Long id,
            @RequestBody Servico servico) {

        /*
         * Solicita à camada Service que realize a atualização.
         *
         * Se o serviço existir:
         * HTTP 200 OK + serviço atualizado.
         *
         * Se não existir:
         * HTTP 404 Not Found.
         */
        return servicoService.atualizar(id, servico)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * DELETE /api/servicos/{id}
     *
     * Desativa um serviço existente.
     *
     * Exemplo:
     * DELETE http://localhost:8080/api/servicos/2
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {

        /*
         * Solicita à camada Service que tente
         * desativar o serviço.
         */
        boolean desativado = servicoService.desativar(id);

        /*
         * Se o serviço foi encontrado e desativado,
         * retornamos HTTP 204 No Content.
         */
        if (desativado) {
            return ResponseEntity.noContent().build();
        }

        /*
         * Se o ID não existir,
         * retornamos HTTP 404 Not Found.
         */
        return ResponseEntity.notFound().build();
    }

}