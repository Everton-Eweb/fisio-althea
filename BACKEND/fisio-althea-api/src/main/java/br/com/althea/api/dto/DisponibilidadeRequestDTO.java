package br.com.althea.api.dto;

import java.time.LocalTime;

import br.com.althea.api.entity.DiaSemana;
import jakarta.validation.constraints.NotNull;

/*
 * DTO utilizado para receber os dados necessários
 * para cadastrar a disponibilidade de um profissional.
 *
 * Ele representa os dados enviados pelo cliente
 * para a API.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Modelagem de Dados
 */
public class DisponibilidadeRequestDTO {

    /*
     * Identificador do profissional ao qual
     * essa disponibilidade pertence.
     *
     * O campo é obrigatório porque toda disponibilidade
     * precisa estar relacionada a um profissional.
     */
    @NotNull(message = "O profissional é obrigatório.")
    private Long idProfissional;


    /*
     * Dia da semana em que o profissional
     * estará disponível.
     *
     * Os valores possíveis são definidos
     * pelo enum DiaSemana:
     *
     * SEGUNDA
     * TERCA
     * QUARTA
     * QUINTA
     * SEXTA
     * SABADO
     */
    @NotNull(message = "O dia da semana é obrigatório.")
    private DiaSemana diaSemana;


    /*
     * Horário em que inicia a disponibilidade.
     *
     * Exemplo:
     * 08:00
     */
    @NotNull(message = "O horário inicial é obrigatório.")
    private LocalTime horaInicio;


    /*
     * Horário em que termina a disponibilidade.
     *
     * Exemplo:
     * 12:00
     */
    @NotNull(message = "O horário final é obrigatório.")
    private LocalTime horaFim;


    /*
     * Construtor vazio necessário para que o Spring
     * consiga transformar o JSON recebido em objeto Java.
     */
    public DisponibilidadeRequestDTO() {
    }


    public Long getIdProfissional() {
        return idProfissional;
    }

    public void setIdProfissional(Long idProfissional) {
        this.idProfissional = idProfissional;
    }


    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }


    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }


    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }
}