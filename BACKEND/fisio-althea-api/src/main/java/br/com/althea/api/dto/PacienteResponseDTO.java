package br.com.althea.api.dto;

import java.time.LocalDate;

/*
 * DTO utilizado para devolver os dados do paciente pela API.
 *
 * DTO significa Data Transfer Object.
 *
 * Ele serve para controlar quais informações serão
 * enviadas para o front-end.
 *
 * Por exemplo: não queremos devolver a senha do usuário
 * junto com os dados do paciente.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - APIs REST
 */
public class PacienteResponseDTO {

    // Identificador do paciente
    private Long idPaciente;

    // Identificador do usuário relacionado ao paciente
    private Long idUsuario;

    // Dados vindos da tabela usuario
    private String nome;
    private String email;

    // Dados específicos do paciente
    private String cpf;
    private LocalDate dataNascimento;
    private String telefone;
    private String endereco;

    /*
     * Construtor utilizado para montar o objeto
     * que será retornado pela API.
     */
    public PacienteResponseDTO(
            Long idPaciente,
            Long idUsuario,
            String nome,
            String email,
            String cpf,
            LocalDate dataNascimento,
            String telefone,
            String endereco) {

        this.idPaciente = idPaciente;
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
    }

    public Long getIdPaciente() {
        return idPaciente;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }
}