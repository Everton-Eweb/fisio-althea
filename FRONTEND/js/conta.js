/* ==================================================
   ÁREA DO PACIENTE — GET /api/pacientes/me
   ================================================== */

const mensagemConta = document.querySelector("#mensagemConta");
const cardPaciente = document.querySelector("#cardPaciente");
const botaoSair = document.querySelector("#botaoSair");

/*
 * Verifica se o usuário está autenticado
 * e possui o perfil PACIENTE.
 *
 * Áreas curriculares:
 * - Desenvolvimento Web
 * - Segurança de Aplicações
 * - APIs REST
 */
if (ControleAcesso.protegerPagina("PACIENTE")) {
    carregarMeusDados();
}

if (botaoSair) {
    botaoSair.addEventListener("click", function () {
        API.encerrarSessao();
        window.location.href = "index.html";
    });
}


async function carregarMeusDados() {

    exibirMensagem("Carregando seus dados...", null);

    try {
        /*
            GET /api/pacientes/me
            Exige token de um usuário com perfil PACIENTE.
        */
        const paciente = await API.requisitar("/pacientes/me", {
            metodo: "GET",
            autenticado: true
        });

        preencherCard(paciente);

        exibirMensagem(null);
        cardPaciente.classList.remove("d-none");

    } catch (erro) {

        /*
         * HTTP 401:
         * O api.js já encerra a sessão e redireciona
         * o usuário para a página de login.
         *
         * Evitamos repetir o redirecionamento aqui.
         *
         * Áreas curriculares:
         * - APIs REST
         * - Segurança de Aplicações
         * - Arquitetura de Software
         */
        if (erro.status === 401) {
            return;
        }

        if (erro.status === 403) {
            exibirMensagem(
                "Esta área é exclusiva para contas de paciente.",
                "erro"
            );
            return;
        }

        exibirMensagem(erro.message, "erro");
    }
}


function preencherCard(paciente) {

    document.querySelector("#campoNome").textContent = paciente.nome || "—";
    document.querySelector("#campoEmail").textContent = paciente.email || "—";
    document.querySelector("#campoCpf").textContent = paciente.cpf || "—";
    document.querySelector("#campoTelefone").textContent = paciente.telefone || "—";
    document.querySelector("#campoEndereco").textContent = paciente.endereco || "—";

    document.querySelector("#campoDataNascimento").textContent =
        paciente.dataNascimento
            ? formatarData(paciente.dataNascimento)
            : "—";
}

/*
    Converte "1990-05-20" (formato da API) para "20/05/1990".
*/
function formatarData(dataIso) {
    const [ano, mes, dia] = dataIso.split("-");
    return `${dia}/${mes}/${ano}`;
}

function exibirMensagem(texto, tipo) {

    if (!texto) {
        mensagemConta.classList.add("d-none");
        mensagemConta.textContent = "";
        return;
    }

    mensagemConta.textContent = texto;
    mensagemConta.classList.remove("d-none");

    mensagemConta.classList.toggle("alerta-erro-althea", tipo === "erro");
}
