package br.com.althea.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * Representa uma mensagem trocada entre
 * um paciente e a Clínica Althea.
 *
 * Esta classe corresponde à tabela "mensagem"
 * criada anteriormente no MySQL.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 */
@Entity
@Table(name = "mensagem")
public class Mensagem {

    /*
     * Identificador único da mensagem.
     *
     * GenerationType.IDENTITY informa que o ID
     * será gerado pelo AUTO_INCREMENT do MySQL.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensagem")
    private Long idMensagem;


    /*
     * Paciente relacionado à mensagem.
     *
     * Um paciente pode possuir várias mensagens,
     * portanto a relação é ManyToOne.
     *
     * A chave estrangeira utilizada no banco
     * é id_paciente.
     */
    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;


    /*
     * Usuário da clínica relacionado à mensagem.
     *
     * Um usuário pode estar relacionado a várias mensagens.
     *
     * A ligação é realizada pela coluna id_usuario.
     */
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;


    /*
     * Assunto da mensagem.
     *
     * O campo é opcional no banco de dados,
     * por isso não usamos nullable = false.
     */
    @Column(name = "assunto", length = 150)
    private String assunto;


    /*
     * Conteúdo principal da mensagem.
     *
     * No MySQL este campo foi criado como TEXT.
     */
    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;


    /*
     * Data e hora em que a mensagem foi registrada.
     *
     * O próprio MySQL gera esse valor utilizando
     * DEFAULT CURRENT_TIMESTAMP.
     *
     * Por isso utilizamos insertable = false.
     */
    @Column(
        name = "data_envio",
        nullable = false,
        insertable = false,
        updatable = false
    )
    private LocalDateTime dataEnvio;


    /*
     * Indica se a mensagem já foi lida.
     *
     * FALSE = ainda não foi lida
     * TRUE  = já foi lida
     */
    @Column(name = "lida", nullable = false)
    private Boolean lida;


    /*
     * Construtor vazio exigido pelo JPA/Hibernate.
     */
    public Mensagem() {
    }


    // Retorna o identificador da mensagem
    public Long getIdMensagem() {
        return idMensagem;
    }

    public void setIdMensagem(Long idMensagem) {
        this.idMensagem = idMensagem;
    }


    // Retorna o paciente relacionado à mensagem
    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
    }


    // Retorna o usuário da clínica relacionado à mensagem
    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }


    // Retorna o assunto da mensagem
    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }


    // Retorna o conteúdo da mensagem
    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }


    // Retorna a data e hora em que a mensagem foi registrada
    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }


    // Retorna se a mensagem já foi lida
    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }
}