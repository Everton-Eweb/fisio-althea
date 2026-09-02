package br.com.althea.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.Profissional;

import java.util.Optional;
/*
 * Responsável pelo acesso aos dados dos profissionais.
 *
 * Profissional representa a entidade manipulada
 * e Long representa o tipo da chave primária.
 */
public interface ProfissionalRepository
                extends JpaRepository<Profissional, Long> {
        /*
         * Busca o profissional através do ID
         * do usuário relacionado.
         *
         * Esse método será utilizado para que
         * o profissional autenticado consiga
         * acessar somente os próprios dados.
         *
         * Exemplo:
         *
         * Usuario:
         * idUsuario = 10
         *
         * Profissional:
         * idUsuario = 10
         *
         * Resultado:
         * retorna aquele profissional.
         *
         * Tópicos:
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Segurança
         * - Modelagem de Dados
         */
        Optional<Profissional> findByUsuario_IdUsuario(Long idUsuario);

}