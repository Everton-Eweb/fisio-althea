package br.com.althea.api.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Representa um agendamento realizado na Clínica Althea.
 *
 * Esta é uma das principais entidades do sistema, pois
 * relaciona:
 *
 * - paciente;
 * - profissional;
 * - serviço;
 * - data;
 * - horário;
 * - status.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 * - Arquitetura de Software
 */
@Entity
@Table(name = "agendamento")
public class Agendamento {

    /*
     * Chave primária da tabela agendamento.
     *
     * IDENTITY corresponde ao AUTO_INCREMENT
     * configurado no MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agendamento")
    private Long idAgendamento;


    /*
     * Relacionamento com o paciente.
     *
     * Um paciente pode possuir vários agendamentos,
     * mas cada agendamento pertence a um paciente.
     *
     * No banco, a ligação ocorre pela FK id_paciente.
     */
    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;


    /*
     * Relacionamento com o profissional.
     *
     * Um profissional pode possuir vários agendamentos.
     */
    @ManyToOne
    @JoinColumn(name = "id_profissional", nullable = false)
    private Profissional profissional;


    /*
     * Relacionamento com o serviço escolhido
     * para o atendimento.
     */
    @ManyToOne
    @JoinColumn(name = "id_servico", nullable = false)
    private Servico servico;


    /*
     * Data em que o atendimento foi agendado.
     *
     * LocalDate corresponde ao tipo DATE do MySQL.
     */
    @Column(name = "data", nullable = false)
    private LocalDate data;


    /*
     * Horário do agendamento.
     *
     * LocalTime corresponde ao tipo TIME do MySQL.
     */
    @Column(name = "hora", nullable = false)
    private LocalTime hora;


    /*
     * Estado atual do agendamento.
     *
     * EnumType.STRING faz o JPA gravar no banco:
     *
     * AGENDADO
     * CONFIRMADO
     * CANCELADO
     *
     * em vez de números.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusAgendamento status;


    /*
     * Campo opcional para informações relacionadas
     * ao agendamento.
     */
    @Column(name = "observacao", length = 255)
    private String observacao;


    /*
     * Guarda a data e hora em que o registro foi criado.
     *
     * insertable = false indica que o próprio MySQL
     * preencherá o valor através do DEFAULT CURRENT_TIMESTAMP
     * que já configuramos no banco.
     *
     * updatable = false impede que essa data seja alterada
     * posteriormente pelo JPA.
     */
    @Column(
        name = "criado_em",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime criadoEm;


    /*
     * Construtor vazio exigido pelo JPA/Hibernate.
     */
    public Agendamento() {
    }


    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public void setIdAgendamento(Long idAgendamento) {
        this.idAgendamento = idAgendamento;
    }


    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }


    public Profissional getProfissional() {
        return profissional;
    }

    public void setProfissional(Profissional profissional) {
        this.profissional = profissional;
    }


    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
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


    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }


    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }


    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}