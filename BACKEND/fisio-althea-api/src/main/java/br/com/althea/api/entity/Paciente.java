package br.com.althea.api.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * Representa um paciente da Clínica Althea.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Entity
@Table(name = "paciente")
public class Paciente {

    /*
     * Chave primária da tabela paciente.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paciente")
    private Long idPaciente;


    /*
     * Relaciona o paciente ao usuário utilizado
     * para acesso ao sistema.
     *
     * id_usuario é a chave estrangeira existente
     * na tabela paciente.
     */
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;


    @Column(name = "cpf", length = 14)
    private String cpf;


    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;


    @Column(name = "telefone", length = 20)
    private String telefone;


    @Column(name = "endereco", length = 255)
    private String endereco;


    /*
     * Construtor vazio necessário para o JPA.
     */
    public Paciente() {
    }


    public Long getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Long idPaciente) {
        this.idPaciente = idPaciente;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }


    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }


    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }


    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }


    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}