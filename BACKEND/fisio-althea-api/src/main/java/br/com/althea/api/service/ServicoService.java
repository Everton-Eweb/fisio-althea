package br.com.althea.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.althea.api.entity.Servico;
import br.com.althea.api.repository.ServicoRepository;

/*
 * Camada de serviço responsável pelas operações
 * relacionadas aos serviços oferecidos pela Clínica Althea.
 *
 * A Service fica entre o Controller e o Repository.
 *
 * Controller
 *      ↓
 * Service
 *      ↓
 * Repository
 *      ↓
 * Banco de Dados
 *
 * Tópicos:
 * - Arquitetura de Software
 * - Spring Framework
 * - Spring Data JPA
 * - Programação Orientada a Objetos
 */
@Service
public class ServicoService {

    /*
     * Repository utilizado para acessar a tabela "servico"
     * no banco de dados.
     */
    private final ServicoRepository servicoRepository;

    /*
     * Injeção de dependência através do construtor.
     *
     * O Spring cria automaticamente uma implementação
     * de ServicoRepository e entrega esse objeto para
     * a classe ServicoService.
     *
     * Dessa forma, a Service não precisa criar o
     * Repository manualmente.
     */
    public ServicoService(ServicoRepository servicoRepository) {
        this.servicoRepository = servicoRepository;
    }

    /*
     * Retorna todos os serviços cadastrados.
     *
     * O método findAll() já é fornecido pelo
     * JpaRepository.
     */
    public List<Servico> listarTodos() {
        return servicoRepository.findAll();
    }

    /*
     * Busca um serviço pelo seu identificador.
     *
     * Optional é utilizado porque o serviço procurado
     * pode existir ou não no banco de dados.
     */
    public Optional<Servico> buscarPorId(Long id) {
        return servicoRepository.findById(id);
    }

    /*
     * Salva um novo serviço ou atualiza um serviço
     * já existente.
     *
     * O método save() também é fornecido pelo
     * JpaRepository.
     */
    public Servico salvar(Servico servico) {
        return servicoRepository.save(servico);
    }

    /*
     * Atualiza um serviço já existente.
     *
     * Primeiro buscamos o serviço pelo ID.
     * Se ele existir, atualizamos seus dados e salvamos novamente.
     *
     * Optional é utilizado porque o ID informado
     * pode não existir no banco.
     */
    public Optional<Servico> atualizar(Long id, Servico novosDados) {

        /*
         * Busca o serviço existente no banco.
         */
        return servicoRepository.findById(id)

                /*
                 * O map() só será executado caso
                 * o serviço tenha sido encontrado.
                 */
                .map(servicoExistente -> {

                    /*
                     * Atualizamos os campos do objeto já existente.
                     *
                     * Não alteramos o ID, pois ele identifica
                     * o registro original no banco.
                     */
                    servicoExistente.setNome(novosDados.getNome());
                    servicoExistente.setDescricao(novosDados.getDescricao());
                    servicoExistente.setDuracaoMinutos(novosDados.getDuracaoMinutos());
                    servicoExistente.setValor(novosDados.getValor());
                    servicoExistente.setAtivo(novosDados.getAtivo());

                    /*
                     * Como o objeto já possui ID,
                     * o save() realizará um UPDATE
                     * em vez de um INSERT.
                     */
                    return servicoRepository.save(servicoExistente);
                });
    }

    /*
     * Desativa um serviço existente.
     *
     * Em vez de excluir fisicamente o registro do banco,
     * alteramos o campo "ativo" para false.
     *
     * Dessa forma, mantemos o registro armazenado,
     * mas indicamos que ele não está mais disponível.
     */
    public boolean desativar(Long id) {

        /*
         * Procura o serviço pelo identificador.
         *
         * Se não existir, o Optional estará vazio.
         */
        return servicoRepository.findById(id)

                /*
                 * map() será executado somente se
                 * o serviço for encontrado.
                 */
                .map(servico -> {

                    // Marca o serviço como inativo.
                    servico.setAtivo(false);

                    /*
                     * Salva a alteração no banco.
                     *
                     * Como o objeto já possui ID,
                     * o JPA executará um UPDATE.
                     */
                    servicoRepository.save(servico);

                    // Indica que a operação foi realizada.
                    return true;
                })

                /*
                 * Caso o serviço não exista,
                 * retorna false.
                 */
                .orElse(false);
    }
}