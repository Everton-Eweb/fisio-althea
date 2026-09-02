package br.com.althea.api.dto;

/*
 * DTO utilizado para devolver os dados
 * de um profissional através da API.
 *
 * Ele evita retornar diretamente a entidade Usuario,
 * principalmente informações sensíveis como a senha.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - APIs REST
 */
public class ProfissionalResponseDTO {

    // Identificador do profissional
    private Long idProfissional;

    // Identificador do usuário relacionado
    private Long idUsuario;

    // Dados do usuário
    private String nome;
    private String email;

    // Dados específicos do profissional
    private String crefito;
    private String especialidade;
    private String telefone;

    /*
     * Construtor utilizado para montar
     * a resposta enviada pela API.
     */
    public ProfissionalResponseDTO(
            Long idProfissional,
            Long idUsuario,
            String nome,
            String email,
            String crefito,
            String especialidade,
            String telefone) {

        this.idProfissional = idProfissional;
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.crefito = crefito;
        this.especialidade = especialidade;
        this.telefone = telefone;
    }

    public Long getIdProfissional() {
        return idProfissional;
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

    public String getCrefito() {
        return crefito;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public String getTelefone() {
        return telefone;
    }
}