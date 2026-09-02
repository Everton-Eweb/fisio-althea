package br.com.althea.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/*
 * DTO utilizado para receber as credenciais
 * informadas pelo usuário no momento do login.
 *
 * Não utilizamos diretamente a entidade Usuario,
 * pois o login precisa receber somente:
 *
 * - e-mail;
 * - senha.
 *
 * Tópicos:
 * - Segurança
 * - APIs REST
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - Levantamento e Especificação de Requisitos
 */
public class LoginRequestDTO {

    /*
     * E-mail utilizado para identificar
     * o usuário no sistema.
     */
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "O e-mail informado é inválido.")
    private String email;


    /*
     * Senha informada pelo usuário.
     *
     * Atenção:
     * a senha recebida aqui será comparada
     * posteriormente com o hash BCrypt
     * armazenado no banco.
     */
    @NotBlank(message = "A senha é obrigatória.")
    private String senha;


    /*
     * Construtor vazio utilizado pelo Spring
     * para converter o JSON recebido
     * em um objeto Java.
     */
    public LoginRequestDTO() {
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
}