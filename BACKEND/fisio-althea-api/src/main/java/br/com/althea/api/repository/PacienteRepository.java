package br.com.althea.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.Paciente;

import java.util.Optional;

/*
 * Responsável pelas operações de acesso aos dados
 * da entidade Paciente.
 *
 * Ao estender JpaRepository, o Spring Data JPA
 * fornece automaticamente operações como:
 *
 * - salvar paciente;
 * - listar pacientes;
 * - buscar paciente por ID;
 * - atualizar paciente;
 * - excluir paciente.
 */
public interface PacienteRepository
                extends JpaRepository<Paciente, Long> {

        /*
         * Busca o paciente através do ID do usuário
         * relacionado a ele.
         *
         * Isso será utilizado no endpoint /me,
         * permitindo descobrir qual paciente está
         * associado ao usuário autenticado no JWT.
         *
         * Exemplo:
         *
         * Usuario:
         * idUsuario = 6
         *
         * Paciente:
         * idUsuario = 6
         *
         * Resultado:
         * retorna somente aquele paciente.
         *
         * Tópicos:
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Segurança
         * - Modelagem de Dados
         */
        Optional<Paciente> findByUsuario_IdUsuario(Long idUsuario);
}