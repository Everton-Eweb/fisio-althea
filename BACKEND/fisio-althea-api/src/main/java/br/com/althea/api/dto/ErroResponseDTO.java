package br.com.althea.api.dto;

import java.time.LocalDateTime;

/*
 * DTO utilizado para padronizar as mensagens
 * de erro retornadas pela API.
 *
 * Em vez de devolver o rastreamento interno do Java,
 * a API retornará apenas informações úteis
 * para quem estiver utilizando o sistema.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - APIs REST
 * - Engenharia de Software
 */
public class ErroResponseDTO {

    /*
     * Data e hora em que o erro ocorreu.
     */
    private LocalDateTime timestamp;

    /*
     * Código HTTP retornado.
     *
     * Exemplo:
     * 400
     * 404
     */
    private int status;

    /*
     * Descrição resumida do tipo de erro.
     *
     * Exemplo:
     * "Bad Request"
     */
    private String erro;

    /*
     * Mensagem explicando o motivo do erro.
     *
     * Exemplo:
     * "Já existe um agendamento para este
     * profissional neste horário."
     */
    private String mensagem;


    /*
     * Construtor utilizado para montar
     * a resposta de erro.
     */
    public ErroResponseDTO(
            LocalDateTime timestamp,
            int status,
            String erro,
            String mensagem) {

        this.timestamp = timestamp;
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErro() {
        return erro;
    }

    public String getMensagem() {
        return mensagem;
    }
}