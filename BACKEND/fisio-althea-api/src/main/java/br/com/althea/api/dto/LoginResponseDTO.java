package br.com.althea.api.dto;

import br.com.althea.api.entity.TipoPerfil;

/*
 * DTO utilizado para devolver os dados
 * do usuário após uma autenticação bem-sucedida.
 *
 * Além dos dados básicos do usuário,
 * agora também devolveremos o token JWT.
 *
 * IMPORTANTE:
 * a senha nunca é devolvida pela API.
 *
 * Tópicos:
 * - Segurança
 * - APIs REST
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 */
public class LoginResponseDTO {

    /*
     * Identificador do usuário autenticado.
     */
    private Long idUsuario;

    /*
     * Nome do usuário.
     */
    private String nome;

    /*
     * E-mail utilizado na autenticação.
     */
    private String email;

    /*
     * Perfil do usuário no sistema.
     *
     * Exemplos:
     * ADMINISTRADOR
     * RECEPCIONISTA
     * PROFISSIONAL
     * PACIENTE
     */
    private TipoPerfil perfil;

    /*
     * Token JWT gerado após a autenticação.
     *
     * Esse token será utilizado posteriormente
     * no cabeçalho Authorization das requisições.
     */
    private String token;


    /*
     * Construtor antigo mantido temporariamente
     * para não quebrar o código atual enquanto
     * ajustamos a AutenticacaoService.
     */
    public LoginResponseDTO(
            Long idUsuario,
            String nome,
            String email,
            TipoPerfil perfil) {

        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
    }


    /*
     * Novo construtor utilizado quando
     * já possuímos o token JWT.
     */
    public LoginResponseDTO(
            Long idUsuario,
            String nome,
            String email,
            TipoPerfil perfil,
            String token) {

        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.perfil = perfil;
        this.token = token;
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

    public TipoPerfil getPerfil() {
        return perfil;
    }

    public String getToken() {
        return token;
    }
}