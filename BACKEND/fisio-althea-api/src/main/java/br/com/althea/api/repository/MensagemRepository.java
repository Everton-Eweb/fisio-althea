package br.com.althea.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.Mensagem;

import java.util.List;

/*
 * Responsável pelo acesso aos dados das mensagens
 * trocadas entre os pacientes e a clínica.
 */
public interface MensagemRepository
                extends JpaRepository<Mensagem, Long> {

        /*
         * Busca as mensagens relacionadas ao paciente
         * vinculado ao usuário autenticado.
         *
         * Fluxo:
         *
         * Usuario
         * ↓
         * Paciente
         * ↓
         * Mensagens relacionadas ao paciente
         *
         * Esse método permitirá que um PACIENTE
         * consulte somente suas próprias mensagens.
         *
         * Tópicos:
         * - Segurança
         * - Spring Data JPA
         * - Banco de Dados Relacional
         * - Modelagem de Dados
         */
        List<Mensagem> findByPaciente_Usuario_IdUsuario(
                        Long idUsuario);
}
