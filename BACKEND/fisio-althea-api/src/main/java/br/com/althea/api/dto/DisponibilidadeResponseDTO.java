package br.com.althea.api.dto;

import java.time.LocalTime;

import br.com.althea.api.entity.DiaSemana;

/*
 * DTO utilizado para devolver os dados
 * de uma disponibilidade através da API.
 *
 * Em vez de retornar diretamente a entidade
 * Disponibilidade, selecionamos apenas os dados
 * necessários para a resposta.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Modelagem de Dados
 */
public class DisponibilidadeResponseDTO {

    /*
     * Identificador da disponibilidade cadastrada.
     */
    private Long idDisponibilidade;


    /*
     * Identificador do profissional relacionado.
     *
     * Retornamos apenas o ID do profissional,
     * evitando expor o objeto Usuario associado a ele.
     */
    private Long idProfissional;


    /*
     * Dia da semana cadastrado.
     */
    private DiaSemana diaSemana;


    /*
     * Horário inicial da disponibilidade.
     */
    private LocalTime horaInicio;


    /*
     * Horário final da disponibilidade.
     */
    private LocalTime horaFim;


    /*
     * Indica se essa disponibilidade está ativa.
     */
    private Boolean ativo;


    /*
     * Construtor utilizado para montar
     * a resposta que será enviada pela API.
     */
    public DisponibilidadeResponseDTO(
            Long idDisponibilidade,
            Long idProfissional,
            DiaSemana diaSemana,
            LocalTime horaInicio,
            LocalTime horaFim,
            Boolean ativo) {

        this.idDisponibilidade = idDisponibilidade;
        this.idProfissional = idProfissional;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.ativo = ativo;
    }


    public Long getIdDisponibilidade() {
        return idDisponibilidade;
    }


    public Long getIdProfissional() {
        return idProfissional;
    }


    public DiaSemana getDiaSemana() {
        return diaSemana;
    }


    public LocalTime getHoraInicio() {
        return horaInicio;
    }


    public LocalTime getHoraFim() {
        return horaFim;
    }


    public Boolean getAtivo() {
        return ativo;
    }
}