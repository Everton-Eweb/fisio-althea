package br.com.althea.api.dto;

import java.time.LocalDateTime;

/*
 * DTO utilizado para devolver os dados
 * de uma mensagem através da API.
 *
 * Em vez de retornar diretamente a entidade Mensagem,
 * selecionamos somente as informações necessárias.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Modelagem de Dados
 */
public class MensagemResponseDTO {

    /*
     * Identificador da mensagem.
     */
    private Long idMensagem;


    /*
     * Identificador do paciente relacionado
     * à mensagem.
     */
    private Long idPaciente;


    /*
     * Nome do paciente.
     *
     * Esse dado será recuperado através
     * do Usuario relacionado ao Paciente.
     */
    private String nomePaciente;


    /*
     * Identificador do usuário relacionado
     * à mensagem.
     */
    private Long idUsuario;


    /*
     * Nome do usuário relacionado
     * à mensagem.
     */
    private String nomeUsuario;


    /*
     * Assunto da mensagem.
     */
    private String assunto;


    /*
     * Conteúdo principal da mensagem.
     */
    private String conteudo;


    /*
     * Data e hora em que a mensagem
     * foi registrada no sistema.
     */
    private LocalDateTime dataEnvio;


    /*
     * Indica se a mensagem já foi lida.
     */
    private Boolean lida;


    /*
     * Construtor utilizado para montar
     * a resposta enviada pela API.
     */
    public MensagemResponseDTO(
            Long idMensagem,
            Long idPaciente,
            String nomePaciente,
            Long idUsuario,
            String nomeUsuario,
            String assunto,
            String conteudo,
            LocalDateTime dataEnvio,
            Boolean lida) {

        this.idMensagem = idMensagem;
        this.idPaciente = idPaciente;
        this.nomePaciente = nomePaciente;
        this.idUsuario = idUsuario;
        this.nomeUsuario = nomeUsuario;
        this.assunto = assunto;
        this.conteudo = conteudo;
        this.dataEnvio = dataEnvio;
        this.lida = lida;
    }


    public Long getIdMensagem() {
        return idMensagem;
    }


    public Long getIdPaciente() {
        return idPaciente;
    }


    public String getNomePaciente() {
        return nomePaciente;
    }


    public Long getIdUsuario() {
        return idUsuario;
    }


    public String getNomeUsuario() {
        return nomeUsuario;
    }


    public String getAssunto() {
        return assunto;
    }


    public String getConteudo() {
        return conteudo;
    }


    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }


    public Boolean getLida() {
        return lida;
    }
}