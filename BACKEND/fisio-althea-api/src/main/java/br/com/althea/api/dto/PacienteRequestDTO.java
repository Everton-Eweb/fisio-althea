package br.com.althea.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * DTO utilizado para RECEBER os dados necessários
 * para cadastrar um novo paciente.
 *
 * Diferente do PacienteResponseDTO, que é usado
 * para devolver dados, este DTO representa os dados
 * que chegam para a API.
 *
 * Tópicos:
 * - APIs REST
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - Validação de Dados
 */
public class PacienteRequestDTO {

    /*
     * Nome do usuário/paciente.
     *
     * @NotBlank impede valores nulos,
     * vazios ou contendo apenas espaços.
     */
    @NotBlank(message = "O nome é obrigatório.")
    private String nome;


    /*
     * Email utilizado pelo usuário.
     *
     * @Email verifica se o texto possui
     * um formato válido de endereço de e-mail.
     */
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;


    /*
     * Senha informada no cadastro.
     *
     * IMPORTANTE:
     * esta senha NÃO será gravada diretamente no banco.
     *
     * Antes de salvar o usuário, vamos transformá-la
     * em um hash seguro.
     */
    @NotBlank(message = "A senha é obrigatória.")
    @Size(
        min = 6,
        message = "A senha deve possuir pelo menos 6 caracteres."
    )
    private String senha;


    /*
     * Dados específicos do paciente.
     */
    private String cpf;

    private LocalDate dataNascimento;

    private String telefone;

    private String endereco;


    /*
     * Construtor vazio necessário para que o Spring
     * consiga converter o JSON recebido em um objeto Java.
     */
    public PacienteRequestDTO() {
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