package br.com.althea.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/*
 * DTO utilizado para RECEBER os dados necessários
 * para criar um novo agendamento.
 *
 * O cliente da API informa:
 *
 * - paciente;
 * - profissional;
 * - serviço;
 * - data;
 * - horário;
 * - observação, quando necessária.
 *
 * O status NÃO é recebido pelo cliente.
 * Um novo agendamento será criado automaticamente
 * com o status AGENDADO.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Modelagem de Dados
 * - Levantamento e Especificação de Requisitos
 */
public class AgendamentoRequestDTO {

    /*
     * Identificador do paciente que realizará
     * o atendimento.
     */
    @NotNull(message = "O paciente é obrigatório.")
    private Long idPaciente;


    /*
     * Identificador do profissional responsável
     * pelo atendimento.
     */
    @NotNull(message = "O profissional é obrigatório.")
    private Long idProfissional;


    /*
     * Identificador do serviço escolhido.
     */
    @NotNull(message = "O serviço é obrigatório.")
    private Long idServico;


    /*
     * Data escolhida para o agendamento.
     *
     * LocalDate corresponde ao tipo DATE
     * utilizado no banco MySQL.
     */
    @NotNull(message = "A data é obrigatória.")
    private LocalDate data;


    /*
     * Horário escolhido para o atendimento.
     *
     * LocalTime corresponde ao tipo TIME
     * utilizado no MySQL.
     */
    @NotNull(message = "O horário é obrigatório.")
    private LocalTime hora;


    /*
     * Informação opcional relacionada
     * ao agendamento.
     */
    private String observacao;


    /*
     * Construtor vazio necessário para que
     * o Spring transforme o JSON recebido
     * em um objeto Java.
     */
    public AgendamentoRequestDTO() {
    }


    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }


    public Long getIdProfissional() {
        return idProfissional;
    }

    public void setIdProfissional(Long idProfissional) {
        this.idProfissional = idProfissional;
    }


    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }


    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }


    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }


    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}