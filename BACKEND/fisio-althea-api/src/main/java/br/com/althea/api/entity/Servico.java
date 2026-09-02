package br.com.althea.api.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * Representa um serviço oferecido pela Clínica Althea.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Entity
@Table(name = "servico")
public class Servico {

    /*
     * Chave primária da tabela servico.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servico")
    private Long idServico;


    /*
     * Nome do serviço.
     * Exemplo: Fisioterapia, Pilates.
     */
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;


    /*
     * Descrição opcional do serviço.
     */
    @Column(name = "descricao", length = 255)
    private String descricao;


    /*
     * Duração prevista do serviço em minutos.
     */
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos;


    /*
     * Valor do serviço.
     *
     * BigDecimal é utilizado para valores monetários
     * em vez de double.
     */
    @Column(name = "valor", precision = 10, scale = 2)
    private BigDecimal valor;


    /*
     * Indica se o serviço está disponível no sistema.
     */
    @Column(name = "ativo", nullable = false)
    private Boolean ativo;


    /*
     * Construtor vazio necessário para o JPA.
     */
    public Servico() {
    }


    public Long getIdServico() {
        return idServico;
    }

    public void setIdServico(Long idServico) {
        this.idServico = idServico;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }


    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }


    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }


    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }


    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}