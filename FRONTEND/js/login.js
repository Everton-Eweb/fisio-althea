/* ==================================================
   FORMULÁRIO DE LOGIN
   ================================================== */

const formularioLogin = document.querySelector("#formLogin");
const mensagemLogin = document.querySelector("#mensagemLogin");
const botaoLogin = document.querySelector("#botaoLogin");

/*
    Se o usuário já estiver autenticado e acessar
    esta página, mandamos direto para a área da conta.
*/
if (API.estaAutenticado()) {
    window.location.href = "minha-conta.html";
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
            const resposta = await API.requisitar("/api/auth/login", {
                metodo: "POST",
                corpo: { email, senha }
            });

            /*
                Guarda o token JWT e os dados básicos
                do usuário no localStorage.
            */
            API.salvarSessao(resposta);

            exibirMensagem("Login realizado com sucesso! Redirecionando...", "sucesso");

            setTimeout(function () {
                window.location.href = "minha-conta.html";
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
