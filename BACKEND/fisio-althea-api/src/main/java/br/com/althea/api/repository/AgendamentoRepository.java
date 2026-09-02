package br.com.althea.api.repository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.Agendamento;
import br.com.althea.api.entity.StatusAgendamento;

import java.util.List;

/*
 * Repository responsável pelo acesso aos dados
 * dos agendamentos.
 *
 * Além das operações padrão do JpaRepository,
 * esta interface também permite verificar
 * conflitos de horário.
 *
 * Tópicos:
 * - Spring Data JPA
 * - Banco de Dados Relacional
 * - Modelagem de Dados
 * - Arquitetura de Software
 * - Regras de Negócio
 */
public interface AgendamentoRepository
                extends JpaRepository<Agendamento, Long> {

        /*
         * Verifica se já existe um agendamento
         * para o mesmo profissional, data e horário.
         *
         * Agendamentos CANCELADOS são ignorados,
         * pois o horário volta a ficar disponível.
         *
         * Retorno:
         *
         * true -> existe conflito
         * false -> horário não está ocupado
         */
        boolean existsByProfissional_IdProfissionalAndDataAndHoraAndStatusNot(
                        Long idProfissional,
                        LocalDate data,
                        LocalTime hora,
                        StatusAgendamento status);

        /*
         * Verifica se existe OUTRO agendamento para
         * o mesmo profissional, data e horário.
         *
         * O ID informado é ignorado na consulta.
         *
         * Isso é necessário no reagendamento para que
         * o próprio agendamento que está sendo alterado
         * não seja considerado como conflito.
         *
         * Agendamentos CANCELADOS também não bloqueiam
         * o novo horário.
         *
         * Tópicos:
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Engenharia de Software
         * - Levantamento e Especificação de Requisitos
         */
        boolean existsByProfissional_IdProfissionalAndDataAndHoraAndStatusNotAndIdAgendamentoNot(
                        Long idProfissional,
                        LocalDate data,
                        LocalTime hora,
                        StatusAgendamento status,
                        Long idAgendamento);

        /*
         * Busca todos os agendamentos relacionados
         * ao PACIENTE vinculado ao usuário informado.
         *
         * Exemplo:
         *
         * JWT
         * idUsuario = 6
         *
         * Usuario 6
         * ↓
         * Paciente correspondente
         * ↓
         * Agendamentos daquele paciente
         *
         * Tópicos:
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Segurança
         * - Modelagem de Dados
         */
        List<Agendamento> findByPaciente_Usuario_IdUsuario(
                        Long idUsuario);

        /*
         * Busca todos os agendamentos relacionados
         * ao PROFISSIONAL vinculado ao usuário informado.
         *
         * Exemplo:
         *
         * JWT
         * idUsuario = 8
         *
         * Usuario 8
         * ↓
         * Profissional correspondente
         * ↓
         * Agendamentos daquele profissional
         */
        List<Agendamento> findByProfissional_Usuario_IdUsuario(
                        Long idUsuario);

        /*
         * Busca todos os agendamentos de um profissional
         * em uma determinada data, ignorando aqueles
         * que já foram CANCELADOS.
         *
         * Esse método será utilizado no cálculo
         * dos horários realmente disponíveis.
         *
         * Exemplo:
         *
         * Profissional 3
         * Data 02/09/2026
         *
         * 08:00 → AGENDADO
         * 09:00 → CANCELADO
         * 10:00 → CONFIRMADO
         *
         * O resultado conterá:
         *
         * 08:00
         * 10:00
         *
         * O horário das 09:00 não ocupa mais a agenda.
         *
         * Tópicos:
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Modelagem de Dados
         * - Arquitetura de Software
         */
        List<Agendamento> findByProfissional_IdProfissionalAndDataAndStatusNot(
                        Long idProfissional,
                        LocalDate data,
                        StatusAgendamento status);
}