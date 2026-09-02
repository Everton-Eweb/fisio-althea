package br.com.althea.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/*
 * DTO utilizado para retornar os horários
 * disponíveis de um profissional para uma
 * determinada data e serviço.
 *
 * Ele será utilizado pelo front-end para
 * montar a lista de horários que o paciente
 * poderá escolher no agendamento.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Levantamento e Especificação de Requisitos
 */
public class HorariosDisponiveisResponseDTO {

    /*
     * Profissional consultado.
     */
    private Long idProfissional;

    /*
     * Serviço que será realizado.
     *
     * A duração do serviço será utilizada
     * posteriormente para calcular os horários.
     */
    private Long idServico;

    /*
     * Data escolhida para o atendimento.
     */
    private LocalDate data;

    /*
     * Horários efetivamente livres.
     */
    private List<LocalTime> horarios;


    public HorariosDisponiveisResponseDTO() {
    }


    public HorariosDisponiveisResponseDTO(
            Long idProfissional,
            Long idServico,
            LocalDate data,
            List<LocalTime> horarios) {

        this.idProfissional = idProfissional;
        this.idServico = idServico;
        this.data = data;
        this.horarios = horarios;
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


    public List<LocalTime> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<LocalTime> horarios) {
        this.horarios = horarios;
    }
}