package br.com.althea.api.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * Representa um período em que um profissional
 * está disponível para receber agendamentos.
 *
 * Esta classe corresponde à tabela "disponibilidade"
 * existente no banco MySQL.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Entity
@Table(name = "disponibilidade")
public class Disponibilidade {

    /*
     * Chave primária da tabela.
     *
     * IDENTITY indica que o próprio MySQL
     * gera o valor através do AUTO_INCREMENT.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_disponibilidade")
    private Long idDisponibilidade;


    /*
     * Cada disponibilidade pertence a um profissional.
     *
     * Um profissional pode possuir várias disponibilidades,
     * por isso utilizamos @ManyToOne.
     *
     * A ligação acontece através da coluna
     * id_profissional da tabela disponibilidade.
     */
    @ManyToOne
    @JoinColumn(name = "id_profissional", nullable = false)
    private Profissional profissional;


    /*
     * Armazena o dia da semana.
     *
     * EnumType.STRING faz com que valores como
     * SEGUNDA, TERCA e QUARTA sejam gravados como texto
     * na coluna VARCHAR do MySQL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    private DiaSemana diaSemana;


    /*
     * Horário inicial da disponibilidade.
     *
     * LocalTime corresponde ao tipo TIME do MySQL.
     */
    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;


    /*
     * Horário final da disponibilidade.
     */
    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;


    /*
     * Permite ativar ou desativar uma disponibilidade
     * sem precisar excluir o registro do banco.
     */
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;


    /*
     * O JPA precisa de um construtor vazio
     * para conseguir criar os objetos da entidade.
     */
    public Disponibilidade() {
    }


    // Retorna o identificador da disponibilidade
    public Long getIdDisponibilidade() {
        return idDisponibilidade;
    }

    public void setIdDisponibilidade(Long idDisponibilidade) {
        this.idDisponibilidade = idDisponibilidade;
    }


    // Retorna o profissional relacionado ao horário
    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }


    // Retorna o dia da semana
    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(DiaSemana diaSemana) {
        this.diaSemana = diaSemana;
    }


    // Retorna o horário inicial
    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }


    // Retorna o horário final
    public LocalTime getHoraFim() {
        return horaFim;
    }

    public void setHoraFim(LocalTime horaFim) {
        this.horaFim = horaFim;
    }


    // Informa se essa disponibilidade está ativa
    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}