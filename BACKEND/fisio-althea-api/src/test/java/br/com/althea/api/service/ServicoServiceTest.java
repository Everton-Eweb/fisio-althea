package br.com.althea.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.althea.api.entity.Servico;
import br.com.althea.api.repository.ServicoRepository;

/*
 * Testes unitários das operações relacionadas
 * aos serviços oferecidos pela Clínica Althea.
 *
 * O Repository é simulado com Mockito,
 * portanto nenhum registro real é criado
 * no banco de dados.
 *
 * Tópicos:
 * - Testes de Software
 * - Levantamento e Especificação de Requisitos
 * - Programação Orientada a Objetos
 * - Arquitetura de Software
 * - Spring Data JPA
 */
@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    /*
     * Simula o Repository utilizado pela Service.
     */
    @Mock
    private ServicoRepository servicoRepository;

    /*
     * O Mockito cria a ServicoService
     * utilizando o Repository simulado.
     */
    @InjectMocks
    private ServicoService servicoService;

    /*
     * RF03 - Cadastro de serviços.
     *
     * Deve salvar corretamente um novo serviço
     * recebido pela camada de Service.
     */
    @Test
    void deveSalvarNovoServico() {

        /*
         * -------------------------------------------------
         * 1. CRIA O SERVIÇO
         * -------------------------------------------------
         */
        Servico servico = new Servico();

        servico.setNome(
                "Fisioterapia");

        servico.setDescricao(
                "Sessão de fisioterapia individual");

        servico.setDuracaoMinutos(
                60);

        servico.setValor(
                new BigDecimal("120.00"));

        servico.setAtivo(
                true);

        /*
         * -------------------------------------------------
         * 2. SIMULA O REPOSITORY
         * -------------------------------------------------
         *
         * Quando a Service solicitar o salvamento,
         * o Repository devolverá o mesmo objeto.
         */
        when(servicoRepository.save(servico))
                .thenReturn(servico);

        /*
         * -------------------------------------------------
         * 3. EXECUTA
         * -------------------------------------------------
         */
        Servico resultado = servicoService
                .salvar(servico);

        /*
         * -------------------------------------------------
         * 4. CONFIRMA O SALVAMENTO
         * -------------------------------------------------
         *
         * A Service deve devolver exatamente
         * o objeto retornado pelo Repository.
         */
        assertSame(
                servico,
                resultado);

        /*
         * Confirma também os principais
         * dados enviados para persistência.
         */
        assertEquals(
                "Fisioterapia",
                resultado.getNome());

        assertEquals(
                "Sessão de fisioterapia individual",
                resultado.getDescricao());

        assertEquals(
                60,
                resultado.getDuracaoMinutos());

        assertEquals(
                new BigDecimal("120.00"),
                resultado.getValor());

        assertEquals(
                true,
                resultado.getAtivo());

        /*
         * -------------------------------------------------
         * 5. CONFIRMA A CHAMADA AO REPOSITORY
         * -------------------------------------------------
         *
         * Aqui comprovamos que a Service realmente
         * encaminhou o serviço para persistência.
         */
        verify(servicoRepository)
                .save(servico);
    }

    /*
     * RF03 - Desativação de serviço.
     *
     * O serviço não é excluído fisicamente.
     * O campo ativo deve ser alterado para false
     * e a entidade deve ser salva novamente.
     *
     * Tópicos:
     * - Testes de Software
     * - Levantamento e Especificação de Requisitos
     * - Programação Orientada a Objetos
     * - Spring Data JPA
     * - Banco de Dados Relacional
     */
    @Test
    void deveDesativarServicoExistente() {

        /*
         * -------------------------------------------------
         * 1. SERVIÇO EXISTENTE
         * -------------------------------------------------
         */
        Servico servico = new Servico();

        servico.setAtivo(true);

        /*
         * -------------------------------------------------
         * 2. SIMULA A BUSCA
         * -------------------------------------------------
         *
         * O serviço de ID 1 existe.
         */
        when(servicoRepository.findById(1L))
                .thenReturn(
                        Optional.of(servico));

        /*
         * -------------------------------------------------
         * 3. EXECUTA
         * -------------------------------------------------
         */
        boolean resultado = servicoService
                .desativar(1L);

        /*
         * -------------------------------------------------
         * 4. VERIFICA
         * -------------------------------------------------
         *
         * O método deve informar que conseguiu
         * realizar a operação.
         */
        assertTrue(resultado);

        /*
         * A entidade continua existindo,
         * porém agora está inativa.
         */
        assertFalse(
                servico.getAtivo());

        /*
         * Confirma que a alteração foi enviada
         * novamente para persistência.
         */
        verify(servicoRepository)
                .save(servico);
    }

    /*
     * RF03 - cenário negativo da desativação.
     *
     * Se o serviço informado não existir,
     * o método deve retornar false e não
     * realizar nenhuma operação de persistência.
     *
     * Tópicos:
     * - Testes de Software
     * - Levantamento e Especificação de Requisitos
     * - Programação Orientada a Objetos
     * - Spring Data JPA
     */
    @Test
    void deveRetornarFalseAoDesativarServicoInexistente() {

        /*
         * -------------------------------------------------
         * 1. SIMULA SERVIÇO INEXISTENTE
         * -------------------------------------------------
         *
         * Optional.empty() representa a situação
         * em que nenhum serviço foi encontrado
         * para o ID informado.
         */
        when(servicoRepository.findById(999L))
                .thenReturn(
                        Optional.empty());

        /*
         * -------------------------------------------------
         * 2. EXECUTA
         * -------------------------------------------------
         */
        boolean resultado = servicoService
                .desativar(999L);

        /*
         * -------------------------------------------------
         * 3. VERIFICA
         * -------------------------------------------------
         *
         * Como o serviço não existe,
         * a operação deve informar false.
         */
        assertFalse(resultado);

        /*
         * Nenhuma entidade deve ser salva.
         */
        verify(
                servicoRepository,
                org.mockito.Mockito.never()).save(
                        org.mockito.ArgumentMatchers
                                .any(Servico.class));
    }

}