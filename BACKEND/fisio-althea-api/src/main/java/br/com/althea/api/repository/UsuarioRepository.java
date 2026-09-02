package br.com.althea.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.Usuario;

import java.util.Optional;

/*
 * Repository responsável pelo acesso aos dados
 * da entidade Usuario.
 *
 * O Spring Data JPA cria automaticamente a implementação
 * desta interface durante a execução da aplicação.
 *
 * Tópicos:
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Arquitetura de Software
 */
public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    /*
     * Verifica se já existe um usuário cadastrado
     * com o e-mail informado.
     *
     * Não precisamos escrever SQL manualmente.
     *
     * O Spring Data JPA interpreta o nome:
     *
     * existsByEmail
     *
     * como:
     *
     * "verifique se existe um registro cujo campo
     * email seja igual ao valor informado".
     *
     * Retorno:
     * true -> e-mail já existe
     * false -> e-mail ainda não existe
     */
    boolean existsByEmail(String email);

    /*
     * Busca um usuário pelo e-mail.
     *
     * Optional é utilizado porque pode existir
     * ou não um usuário cadastrado com o
     * endereço informado.
     *
     * Esse método será utilizado no processo
     * de autenticação.
     *
     * Tópicos:
     * - Spring Data JPA
     * - Banco de Dados Relacional
     * - Segurança
     * - Arquitetura de Software
     */
    Optional<Usuario> findByEmail(String email);
}