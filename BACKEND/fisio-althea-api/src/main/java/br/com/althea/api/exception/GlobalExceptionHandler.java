package br.com.althea.api.exception;

import java.time.LocalDateTime;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.althea.api.dto.ErroResponseDTO;

/*
 * Classe responsável pelo tratamento global
 * das exceções geradas pela API.
 *
 * Dessa forma, os Controllers não precisam
 * possuir vários blocos try/catch.
 *
 * Tópicos:
 * - Engenharia de Software
 * - Arquitetura de Software
 * - APIs REST
 * - Spring Framework
 * - Testes de Software
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        /*
         * Trata erros relacionados às regras
         * de negócio da aplicação.
         *
         * Exemplos:
         *
         * - profissional inexistente;
         * - paciente inexistente;
         * - serviço inexistente;
         * - conflito de horário;
         * - horário indisponível.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErroResponseDTO> tratarIllegalArgument(
                        IllegalArgumentException exception) {

                HttpStatus status = HttpStatus.BAD_REQUEST;

                ErroResponseDTO erro = new ErroResponseDTO(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage());

                return ResponseEntity
                                .status(status)
                                .body(erro);
        }

        /*
         * Trata os erros gerados pelas validações
         * dos DTOs através do @Valid.
         *
         * Essa exceção ocorre quando algum campo
         * não atende às regras definidas com
         * 
         * @NotNull, @NotBlank, @Email, @Size etc.
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErroResponseDTO> tratarValidacao(
                        MethodArgumentNotValidException exception) {

                HttpStatus status = HttpStatus.BAD_REQUEST;

                /*
                 * Uma requisição pode possuir vários
                 * campos inválidos ao mesmo tempo.
                 *
                 * Neste momento vamos retornar a primeira
                 * mensagem encontrada para manter
                 * a resposta simples e objetiva.
                 */
                String mensagem = exception
                                .getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .orElse("Dados inválidos.");

                /*
                 * Cria a mesma estrutura padronizada
                 * de resposta utilizada nos outros erros.
                 */
                ErroResponseDTO erro = new ErroResponseDTO(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                mensagem);

                return ResponseEntity
                                .status(status)
                                .body(erro);
        }

        /*
         * Trata situações em que o recurso solicitado
         * não existe no sistema.
         *
         * Exemplos:
         *
         * - paciente não encontrado;
         * - profissional não encontrado;
         * - serviço não encontrado;
         * - agendamento não encontrado;
         * - mensagem não encontrada.
         *
         * Nesse caso, a resposta correta da API
         * será HTTP 404 Not Found.
         *
         * Tópicos:
         * - APIs REST
         * - Spring Framework
         * - Spring Boot
         * - Arquitetura de Software
         * - Engenharia de Software
         */
        @ExceptionHandler(RecursoNaoEncontradoException.class)
        public ResponseEntity<ErroResponseDTO> tratarRecursoNaoEncontrado(
                        RecursoNaoEncontradoException exception) {

                /*
                 * Define o código HTTP apropriado
                 * para recurso inexistente.
                 */
                HttpStatus status = HttpStatus.NOT_FOUND;

                /*
                 * Mantemos a mesma estrutura padronizada
                 * utilizada nos demais erros da API.
                 */
                ErroResponseDTO erro = new ErroResponseDTO(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage());

                return ResponseEntity
                                .status(status)
                                .body(erro);
        }
}