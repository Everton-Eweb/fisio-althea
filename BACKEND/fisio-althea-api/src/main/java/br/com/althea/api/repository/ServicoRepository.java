package br.com.althea.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.althea.api.entity.Servico;

/*
 * Responsável pelas operações de persistência
 * dos serviços oferecidos pela clínica.
 */
public interface ServicoRepository
        extends JpaRepository<Servico, Long> {

}