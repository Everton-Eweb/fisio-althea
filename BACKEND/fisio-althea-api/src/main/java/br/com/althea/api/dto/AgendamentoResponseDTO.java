package br.com.althea.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.com.althea.api.entity.StatusAgendamento;

/*
 * DTO utilizado para devolver os dados
 * de um agendamento através da API.
 *
 * Em vez de retornar diretamente a entidade
 * Agendamento, selecionamos somente as informações
 * necessárias para a resposta.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Modelagem de Dados
 */
public class AgendamentoResponseDTO {

    /*
     * Identificador do agendamento.
     */
    private Long idAgendamento;


    /*
     * Identificador do paciente relacionado.
     */
    private Long idPaciente;


    /*
     * Nome do paciente.
     *
     * Esse dado será obtido através do Usuario
     * relacionado ao Paciente.
     */
    private String nomePaciente;


    /*
     * Identificador do profissional relacionado.
     */
    private Long idProfissional;


    /*
     * Nome do profissional.
     */
    private String nomeProfissional;


    /*
     * Identificador do serviço escolhido.
     */
    private Long idServico;


    /*
     * Nome do serviço.
     */
    private String nomeServico;


    /*
     * Data do agendamento.
     */
    private LocalDate data;


    /*
     * Horário do agendamento.
     */
    private LocalTime hora;


    /*
     * Situação atual do agendamento.
     *
     * Os valores possíveis atualmente são:
     *
     * AGENDADO
     * CONFIRMADO
     * CANCELADO
     */
    private StatusAgendamento status;


    /*
     * Observação opcional relacionada
     * ao agendamento.
     */
    private String observacao;


    /*
     * Data e hora em que o registro
     * foi criado no sistema.
     */
    private LocalDateTime criadoEm;


    /*
     * Construtor utilizado para montar
     * a resposta enviada pela API.
     */
    public AgendamentoResponseDTO(
            Long idAgendamento,
            Long idPaciente,
            String nomePaciente,
            Long idProfissional,
            String nomeProfissional,
            Long idServico,
            String nomeServico,
            LocalDate data,
            LocalTime hora,
            StatusAgendamento status,
            String observacao,
            LocalDateTime criadoEm) {

        this.idAgendamento = idAgendamento;
        this.idPaciente = idPaciente;
        this.nomePaciente = nomePaciente;
        this.idProfissional = idProfissional;
        this.nomeProfissional = nomeProfissional;
        this.idServico = idServico;
        this.nomeServico = nomeServico;
        this.data = data;
        this.hora = hora;
        this.status = status;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
    }


    public Long getIdAgendamento() {
        return idAgendamento;
    }


    public Long getIdPaciente() {
        return idPaciente;
    }


    public String getNomePaciente() {
        return nomePaciente;
    }


    public Long getIdProfissional() {
        return idProfissional;
    }


    public String getNomeProfissional() {
        return nomeProfissional;
    }


    public Long getIdServico() {
        return idServico;
    }


    public String getNomeServico() {
        return nomeServico;
    }


    public LocalDate getData() {
        return data;
    }


    public LocalTime getHora() {
        return hora;
    }


    public StatusAgendamento getStatus() {
        return status;
    }


    public String getObservacao() {
        return observacao;
    }


    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}