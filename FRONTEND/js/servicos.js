/* ==================================================
   SERVIÇOS
   ==================================================

   Responsável por:

   - consultar os serviços cadastrados na API;
   - preparar a exibição dos serviços na área do paciente;
   - preservar o idServico para uso futuro
     no fluxo de agendamento.

   Área curricular:
   - Desenvolvimento Web
   - APIs REST
   - Arquitetura de Software
   ==================================================
*/

document.addEventListener("DOMContentLoaded", carregarServicos);


/*
 * Consulta os serviços cadastrados no backend.
 *
 * Endpoint:
 * GET /api/servicos
 */
async function carregarServicos() {

    const listaServicos =
        document.getElementById("listaServicos");

    const mensagemServicos =
        document.getElementById("mensagemServicos");

    try {

        /*
         * Utiliza a infraestrutura centralizada
         * existente em api.js.
         */
        const servicos =
            await API.requisitar("/servicos");

        /*
         * Remove mensagens anteriores.
         */
        mensagemServicos.classList.add("d-none");
        mensagemServicos.textContent = "";

        /*
         * Limpa a área antes da renderização.
         */
        listaServicos.innerHTML = "";

        /*
         * Caso nenhum serviço tenha sido retornado.
         */
        if (!servicos || servicos.length === 0) {

            mensagemServicos.textContent =
                "Nenhum serviço disponível no momento.";

            mensagemServicos.classList.remove("d-none");

            return;
        }

        /*
         * Percorre os serviços recebidos da API
         * e cria um card para cada registro.
         */
        servicos.forEach(function (servico) {

            const coluna = document.createElement("div");
            coluna.className = "col-12 col-md-6 col-lg-4";

            /*
             * Preserva o identificador do serviço
             * para utilização futura no agendamento.
             */
            coluna.dataset.idServico = servico.idServico;

            const card = document.createElement("div");
            card.className = "formulario-agendamento h-100";

            /*
             * Nome do serviço.
             */
            const titulo = document.createElement("h3");
            titulo.className = "h5 mb-3";
            titulo.textContent =
                servico.nome || "Serviço";

            /*
             * Descrição.
             */
            const descricao = document.createElement("p");
            descricao.className = "mb-2";
            descricao.textContent =
                servico.descricao ||
                "Descrição não informada.";

            /*
             * Duração.
             */
            const duracao = document.createElement("p");
            duracao.className = "mb-2";

            duracao.textContent =
                servico.duracaoMinutos != null
                    ? "Duração: " + servico.duracaoMinutos + " minutos"
                    : "Duração não informada";

            /*
             * Valor.
             */
            const valor = document.createElement("p");
            valor.className = "mb-2";

            if (servico.valor != null) {

                const valorFormatado =
                    Number(servico.valor).toLocaleString(
                        "pt-BR",
                        {
                            style: "currency",
                            currency: "BRL"
                        }
                    );

                valor.textContent =
                    "Valor: " + valorFormatado;

            } else {

                valor.textContent =
                    "Valor não informado";
            }

            /*
             * Situação cadastrada no backend.
             */
            const situacao = document.createElement("p");
            situacao.className = "mb-0";

            situacao.textContent =
                servico.ativo
                    ? "Disponível"
                    : "Indisponível";

            card.appendChild(titulo);
            card.appendChild(descricao);
            card.appendChild(duracao);
            card.appendChild(valor);
            card.appendChild(situacao);

            coluna.appendChild(card);
            listaServicos.appendChild(coluna);
        });

    } catch (erro) {

        console.error(
            "Erro ao carregar serviços:",
            erro
        );

        mensagemServicos.textContent =
            erro.message ||
            "Não foi possível carregar os serviços.";

        mensagemServicos.classList.remove("d-none");
    }
}
