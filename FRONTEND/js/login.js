/* ==================================================
   FORMULÁRIO DE LOGIN
   ================================================== */

const formularioLogin = document.querySelector("#formLogin");
const mensagemLogin = document.querySelector("#mensagemLogin");
const botaoLogin = document.querySelector("#botaoLogin");

/*
 * Verifica se o usuário chegou ao login após
 * tentar acessar uma área sem permissão.
 *
 * Áreas curriculares:
 * - Desenvolvimento Web
 * - Segurança de Aplicações
 */
const parametrosUrl = new URLSearchParams(window.location.search);
const acessoNegado = parametrosUrl.get("acesso") === "negado";

if (acessoNegado) {

    exibirMensagem(
        "Você não possui permissão para acessar essa área. " +
        "Entre com uma conta autorizada para continuar.",
        "erro"
    );

} else if (API.estaAutenticado()) {

    const usuario = API.obterUsuario();

    const paginaDestino = usuario
        ? obterPaginaPorPerfil(usuario.perfil)
        : null;

    if (paginaDestino) {
        window.location.replace(paginaDestino);
    } else {
        API.encerrarSessao();
    }
}

if (formularioLogin) {

    formularioLogin.addEventListener("submit", async function (evento) {

        evento.preventDefault();

        exibirMensagem(null);

        const email = document.querySelector("#email").value.trim();
        const senha = document.querySelector("#senha").value;

        if (!email || !senha) {
            exibirMensagem("Preencha e-mail e senha para continuar.", "erro");
            return;
        }

        alternarCarregamento(true);

        try {
            /*
                POST /api/auth/login

                Endpoint público (não exige token).
            */
            const resposta = await API.requisitar("/auth/login", {
                metodo: "POST",
                corpo: { email, senha }
            });

            /*
            * Identifica a página correspondente ao perfil
            * retornado pelo backend.
            *
            * Áreas curriculares:
            * - Desenvolvimento Web
            * - APIs REST
            * - Segurança de Aplicações
            */
            const paginaDestino = obterPaginaPorPerfil(resposta.perfil);

            /*
            * Impede a criação de uma sessão local
            * quando o perfil não é reconhecido.
            */
            if (!paginaDestino) {
                throw new Error(
                    "Perfil de usuário não reconhecido. Entre em contato com a clínica."
                );
            }

            /*
            * Armazena o token JWT e os dados do usuário.
            */
            API.salvarSessao(resposta);

            exibirMensagem(
                "Login realizado com sucesso! Redirecionando...",
                "sucesso"
            );

            /*
            * Direciona o usuário para a área correspondente.
            */
            setTimeout(function () {
                window.location.href = paginaDestino;
            }, 900);

        } catch (erro) {

            /*
                Credenciais inválidas (401) ou dados
                inválidos (400) chegam aqui com a mensagem
                já tratada pelo módulo api.js.
            */
            if (erro.status === 401 || erro.status === 400) {
                exibirMensagem("E-mail ou senha incorretos.", "erro");
            } else {
                exibirMensagem(erro.message, "erro");
            }

        } finally {
            alternarCarregamento(false);
        }
    });
}


/*
    Exibe (ou esconde) a mensagem de retorno do formulário.

    tipo: "sucesso" | "erro" | null
*/
function exibirMensagem(texto, tipo) {

    if (!texto) {
        mensagemLogin.classList.add("d-none");
        mensagemLogin.textContent = "";
        return;
    }

    mensagemLogin.textContent = texto;
    mensagemLogin.classList.remove("d-none");

    mensagemLogin.classList.toggle("alerta-erro-althea", tipo === "erro");
}

function alternarCarregamento(carregando) {
    botaoLogin.disabled = carregando;
    botaoLogin.textContent = carregando ? "Entrando..." : "Entrar";
}

/*
 * Define a página inicial de cada perfil.
 *
 * Áreas curriculares:
 * - Desenvolvimento Web
 * - Programação Orientada a Objetos
 * - Segurança de Aplicações
 */
function obterPaginaPorPerfil(perfil) {

    switch (perfil) {

        case "PACIENTE":
            return "minha-conta.html";

        case "PROFISSIONAL":
            return "area-profissional.html";

        case "RECEPCIONISTA":
            return "area-recepcao.html";

        case "ADMINISTRADOR":
            return "area-admin.html";

        default:
            return null;
    }
}