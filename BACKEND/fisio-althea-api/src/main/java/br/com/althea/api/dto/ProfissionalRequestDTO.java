package br.com.althea.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * DTO utilizado para RECEBER os dados necessários
 * para cadastrar um profissional da Clínica Althea.
 *
 * Assim como no cadastro de paciente, os dados
 * recebidos serão utilizados para criar:
 *
 * 1. um Usuario;
 * 2. um Profissional relacionado a esse usuário.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - APIs REST
 * - Arquitetura de Software
 * - Validação de Dados
 */
public class ProfissionalRequestDTO {

    /*
     * Nome do profissional.
     *
     * @NotBlank impede que o campo seja enviado
     * vazio, nulo ou apenas com espaços.
     */
    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    /*
     * E-mail utilizado para acesso ao sistema.
     *
     * @Email verifica se o valor possui
     * um formato válido de e-mail.
     */
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;

    /*
     * Senha utilizada no acesso ao sistema.
     *
     * A senha recebida aqui NÃO será armazenada
     * diretamente no banco.
     *
     * Posteriormente o ProfissionalService utilizará
     * o PasswordEncoder para gerar o hash BCrypt.
     */
    @NotBlank(message = "A senha é obrigatória.")
    @Size(min = 6, message = "A senha deve possuir pelo menos 6 caracteres.")
    private String senha;

    /*
     * Dados específicos do profissional.
     *
     * Estes campos correspondem à tabela
     * "profissional" do banco MySQL.
     */
    private String crefito;

    private String especialidade;

    private String telefone;

    /*
     * Construtor vazio necessário para que o Spring
     * consiga transformar o JSON recebido em um
     * objeto ProfissionalRequestDTO.
     */
    public ProfissionalRequestDTO() {
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