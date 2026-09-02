package br.com.althea.api.entity;

import jakarta.persistence.*;

/*
 * Representa a tabela "usuario" do banco de dados.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Modelagem de Dados
 * - Banco de Dados Relacional
 */
@Entity
@Table(name = "usuario")
public class Usuario {

    /*
     * Chave primária da tabela.
     *
     * GenerationType.IDENTITY corresponde ao AUTO_INCREMENT
     * utilizado no MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;


    @Column(name = "nome", nullable = false, length = 150)
    private String nome;


    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;


    @Column(name = "senha", nullable = false, length = 255)
    private String senha;


    /*
     * O enum será armazenado como texto no banco.
     *
     * Exemplo:
     * ADMINISTRADOR
     * RECEPCIONISTA
     * PROFISSIONAL
     * PACIENTE
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 30)
    private TipoPerfil perfil;


    @Column(name = "ativo", nullable = false)
    private Boolean ativo;


    /*
     * Construtor vazio obrigatório para o JPA/Hibernate.
     */
    public Usuario() {
    }


    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }


    public TipoPerfil getPerfil() {
        return perfil;
    }

    public void setPerfil(TipoPerfil perfil) {
        this.perfil = perfil;
    }


    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}