/* ==================================================
   PROFISSIONAIS
   ==================================================

   Responsável por:

   - consultar os profissionais cadastrados na API;
   - exibir os profissionais na área do paciente;
   - consultar os detalhes de um profissional;
   - preservar o idProfissional para uso futuro
     no fluxo de agendamento.

   Área curricular:
   - Desenvolvimento Web
   - APIs REST
   - Arquitetura de Software
   ==================================================
*/

document.addEventListener("DOMContentLoaded", carregarProfissionais);


/*
 * Consulta os profissionais cadastrados no backend.
 *
 * Endpoint:
 * GET /api/profissionais
 */
async function carregarProfissionais() {

    const listaProfissionais =
        document.getElementById("listaProfissionais");

    const mensagemProfissionais =
        document.getElementById("mensagemProfissionais");

    try {

        /*
         * Utilizamos a infraestrutura centralizada
         * existente em api.js.
         *
         * Não fazemos fetch() diretamente aqui.
         */
        const profissionais =
            await API.requisitar("/profissionais");

        /*
         * Remove qualquer mensagem anterior.
         */
        mensagemProfissionais.classList.add("d-none");
        mensagemProfissionais.textContent = "";

        /*
         * Limpa a lista antes de renderizar.
         */
        listaProfissionais.innerHTML = "";

        /*
         * Caso não existam profissionais cadastrados.
         */
        if (!profissionais || profissionais.length === 0) {

            mensagemProfissionais.textContent =
                "Nenhum profissional disponível no momento.";

            mensagemProfissionais.classList.remove("d-none");

            return;
        }

        /*
         * Percorre os profissionais recebidos
         * da API e cria um card para cada registro.
         */
        profissionais.forEach(function (profissional) {

            const coluna = document.createElement("div");
            coluna.className = "col-12 col-md-6 col-lg-4";

            /*
             * Preserva o idProfissional para utilização
             * futura no fluxo de agendamento.
             */
            coluna.dataset.idProfissional =
                profissional.idProfissional;

            const card = document.createElement("div");
            card.className =
                "formulario-agendamento h-100";

            const titulo = document.createElement("h3");
            titulo.className = "h5 mb-3";
            titulo.textContent =
                profissional.nome || "Profissional";

            const especialidade =
                document.createElement("p");

            especialidade.className = "mb-2";
            especialidade.textContent =
                profissional.especialidade ||
                "Especialidade não informada";

            const crefito =
                document.createElement("p");

            crefito.className = "mb-2";
            crefito.textContent =
                profissional.crefito
                    ? "CREFITO: " + profissional.crefito
                    : "CREFITO não informado";

            /*
             * Botão responsável por consultar os
             * detalhes do profissional selecionado.
             */
            const botaoDetalhes =
                document.createElement("button");

            botaoDetalhes.type = "button";
            botaoDetalhes.className =
                "btn btn-outline-secondary mt-2";

            botaoDetalhes.textContent =
                "Ver detalhes";

            /*
             * Utilizamos o idProfissional retornado
             * pelo próprio backend.
             */
            botaoDetalhes.addEventListener(
                "click",
                function () {

                    carregarDetalhesProfissional(
                        profissional.idProfissional
                    );
                }
            );

            card.appendChild(titulo);
            card.appendChild(especialidade);
            card.appendChild(crefito);
            card.appendChild(botaoDetalhes);

            coluna.appendChild(card);
            listaProfissionais.appendChild(coluna);
        });

    } catch (erro) {

        console.error(
            "Erro ao carregar profissionais:",
            erro
        );

        mensagemProfissionais.textContent =
            erro.message ||
            "Não foi possível carregar os profissionais.";

        mensagemProfissionais.classList.remove("d-none");
    }
}


/*
 * Consulta um profissional específico.
 *
 * Endpoint:
 * GET /api/profissionais/{id}
 *
 * O idProfissional utilizado é o identificador
 * retornado pelo backend.
 */
async function carregarDetalhesProfissional(idProfissional) {

    const modalElemento =
        document.getElementById("modalProfissional");

    const mensagem =
        document.getElementById(
            "mensagemDetalhesProfissional"
        );

    const dados =
        document.getElementById(
            "dadosDetalhesProfissional"
        );

    /*
     * Limpa o estado deixado por uma consulta anterior.
     */
    mensagem.classList.add("d-none");
    mensagem.textContent = "";

    dados.classList.remove("d-none");

    /*
     * Estado inicial enquanto aguardamos a API.
     */
    document.getElementById(
        "detalheProfissionalNome"
    ).textContent = "Carregando...";

    document.getElementById(
        "detalheProfissionalEspecialidade"
    ).textContent = "—";

    document.getElementById(
        "detalheProfissionalCrefito"
    ).textContent = "—";

    document.getElementById(
        "detalheProfissionalEmail"
    ).textContent = "—";

    document.getElementById(
        "detalheProfissionalTelefone"
    ).textContent = "—";

    /*
     * Reutilizamos sempre o mesmo Modal Bootstrap.
     */
    const modal =
        bootstrap.Modal.getOrCreateInstance(
            modalElemento
        );

    modal.show();

    try {

        /*
         * Exemplo:
         *
         * idProfissional = 3
         *
         * GET /api/profissionais/3
         */
        const profissional =
            await API.requisitar(
                `/profissionais/${idProfissional}`
            );

        document.getElementById(
            "detalheProfissionalNome"
        ).textContent =
            profissional.nome || "Não informado";

        document.getElementById(
            "detalheProfissionalEspecialidade"
        ).textContent =
            profissional.especialidade ||
            "Não informada";

        document.getElementById(
            "detalheProfissionalCrefito"
        ).textContent =
            profissional.crefito ||
            "Não informado";

        document.getElementById(
            "detalheProfissionalEmail"
        ).textContent =
            profissional.email ||
            "Não informado";

        document.getElementById(
            "detalheProfissionalTelefone"
        ).textContent =
            profissional.telefone ||
            "Não informado";

    } catch (erro) {

        console.error(
            "Erro ao carregar detalhes do profissional:",
            erro
        );

        /*
         * Esconde os campos para não deixar dados
         * antigos aparecendo quando a consulta falhar.
         */
        dados.classList.add("d-none");

        mensagem.textContent =
            erro.message ||
            "Não foi possível carregar os dados do profissional.";

        mensagem.classList.remove("d-none");
    }
}
