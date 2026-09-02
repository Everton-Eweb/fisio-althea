package br.com.althea.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

/*
 * DTO utilizado para receber os dados
 * necessários para reagendar um atendimento.
 *
 * Neste caso não precisamos enviar novamente:
 *
 * - paciente;
 * - profissional;
 * - serviço.
 *
 * Essas informações continuam pertencendo
 * ao agendamento original.
 *
 * O usuário informa somente a nova data
 * e o novo horário.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Levantamento e Especificação de Requisitos
 */
public class ReagendamentoRequestDTO {

    /*
     * Nova data escolhida para o atendimento.
     */
    @NotNull(message = "A nova data é obrigatória.")
    private LocalDate data;


    /*
     * Novo horário escolhido.
     */
    @NotNull(message = "O novo horário é obrigatório.")
    private LocalTime hora;


    /*
     * Construtor vazio utilizado pelo Spring
     * para transformar o JSON recebido
     * em um objeto Java.
     */
    public ReagendamentoRequestDTO() {
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
}