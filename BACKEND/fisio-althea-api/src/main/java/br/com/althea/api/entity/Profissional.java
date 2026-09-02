package br.com.althea.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/*
 * Representa um profissional da Clínica Althea.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Entity
@Table(name = "profissional")
public class Profissional {

    /*
     * Chave primária da tabela profissional.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profissional")
    private Long idProfissional;


    /*
     * Relaciona o profissional ao usuário
     * utilizado para acesso ao sistema.
     *
     * A coluna id_usuario é uma chave estrangeira
     * existente na tabela profissional.
     */
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;


    @Column(name = "crefito", length = 30)
    private String crefito;


    @Column(name = "especialidade", length = 100)
    private String especialidade;


    @Column(name = "telefone", length = 20)
    private String telefone;


    /*
     * Construtor vazio necessário para o JPA.
     */
    public Profissional() {
    }


    public Long getIdProfissional() {
        return idProfissional;
    }

    public void setIdProfissional(Long idProfissional) {
        this.idProfissional = idProfissional;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }


    public String getCrefito() {
        return crefito;
    }

    public void setCrefito(String crefito) {
        this.crefito = crefito;
    }


    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }


    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}