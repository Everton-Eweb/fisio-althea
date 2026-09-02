package br.com.althea.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.DiaSemana;
import br.com.althea.api.entity.Disponibilidade;

/*
 * Repository responsável pelo acesso aos dados
 * das disponibilidades dos profissionais.
 *
 * Tópicos:
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 * - Arquitetura de Software
 */
public interface DisponibilidadeRepository
        extends JpaRepository<Disponibilidade, Long> {

    /*
     * Busca todas as disponibilidades ATIVAS
     * de um profissional para determinado dia da semana.
     *
     * Exemplo:
     *
     * profissional = 2
     * diaSemana = SEGUNDA
     *
     * O Spring Data JPA interpreta automaticamente
     * o nome do método e cria a consulta necessária.
     */
    List<Disponibilidade>
        findByProfissional_IdProfissionalAndDiaSemanaAndAtivoTrue(
            Long idProfissional,
            DiaSemana diaSemana
        );
}