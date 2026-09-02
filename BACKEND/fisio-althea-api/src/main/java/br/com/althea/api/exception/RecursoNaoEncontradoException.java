package br.com.althea.api.exception;

/*
 * Exceção utilizada quando um recurso solicitado
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
 * Essa exceção permitirá ao
 * GlobalExceptionHandler retornar
 * HTTP 404 Not Found.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - APIs REST
 * - Engenharia de Software
 */
public class RecursoNaoEncontradoException
        extends RuntimeException {

    /*
     * Recebe a mensagem que será posteriormente
     * devolvida pela API.
     *
     * Exemplo:
     * "Agendamento não encontrado."
     */
    public RecursoNaoEncontradoException(
            String mensagem) {

        super(mensagem);
    }
}