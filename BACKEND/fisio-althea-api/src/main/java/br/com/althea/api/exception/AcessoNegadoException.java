package br.com.althea.api.exception;

/**
 * Exceção utilizada quando o usuário autenticado
 * não possui permissão para acessar um recurso.
 */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}