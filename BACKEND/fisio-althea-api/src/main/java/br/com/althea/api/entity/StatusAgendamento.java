package br.com.althea.api.entity;

/*
 * Define os possíveis estados de um agendamento.
 *
 * Utilizamos enum porque o status deve possuir apenas
 * valores previamente definidos pelo sistema.
 *
 * Tópicos:
 * - Programação Orientada a Objetos
 * - Modelagem de Dados
 * - Regras de Negócio
 */
public enum StatusAgendamento {

    // Agendamento criado, mas ainda não confirmado
    AGENDADO,

    // Agendamento confirmado
    CONFIRMADO,

    // Agendamento cancelado
    CANCELADO

}