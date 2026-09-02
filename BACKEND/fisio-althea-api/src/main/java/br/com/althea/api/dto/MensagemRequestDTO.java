package br.com.althea.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/*
 * DTO utilizado para receber os dados necessários
 * para cadastrar uma mensagem no sistema.
 *
 * A entidade Mensagem possui relacionamento com:
 *
 * - Paciente;
 * - Usuario.
 *
 * A data de envio e o status de leitura serão
 * controlados pelo próprio sistema.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Modelagem de Dados
 * - Levantamento e Especificação de Requisitos
 */
public class MensagemRequestDTO {

    /*
     * Identificador do paciente relacionado
     * à mensagem.
     */
    @NotNull(message = "O paciente é obrigatório.")
    private Long idPaciente;


    /*
     * Assunto da mensagem.
     *
     * No banco esse campo pode ser opcional,
     * portanto não utilizamos @NotBlank aqui.
     */
    private String assunto;


    /*
     * Conteúdo principal da mensagem.
     *
     * Este campo é obrigatório.
     */
    @NotBlank(message = "O conteúdo da mensagem é obrigatório.")
    private String conteudo;


    /*
     * Construtor vazio utilizado pelo Spring
     * para transformar o JSON recebido
     * em um objeto Java.
     */
    public MensagemRequestDTO() {
    }


    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }


    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }


    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}