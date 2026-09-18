/* ==================================================
   FORMULÁRIO DE CADASTRO DE PACIENTE
   ================================================== */

const formularioCadastro = document.querySelector("#formCadastro");
const mensagemCadastro = document.querySelector("#mensagemCadastro");
const botaoCadastro = document.querySelector("#botaoCadastro");

if (API.estaAutenticado()) {
    window.location.href = "minha-conta.html";
}

if (formularioCadastro) {

    formularioCadastro.addEventListener("submit", async function (evento) {

        evento.preventDefault();

        exibirMensagem(null);

        const nome = document.querySelector("#nome").value.trim();
        const email = document.querySelector("#email").value.trim();
        const senha = document.querySelector("#senha").value;
        const confirmarSenha = document.querySelector("#confirmarSenha").value;
        const telefone = document.querySelector("#telefone").value.trim();
        const dataNascimento = document.querySelector("#dataNascimento").value;
        const cpf = document.querySelector("#cpf").value.trim();
        const endereco = document.querySelector("#endereco").value.trim();

        if (!nome || !email || !senha) {
            exibirMensagem("Preencha nome, e-mail e senha para continuar.", "erro");
            return;
        }

        if (senha.length < 6) {
            exibirMensagem("A senha deve possuir pelo menos 6 caracteres.", "erro");
            return;
        }

        if (senha !== confirmarSenha) {
            exibirMensagem("As senhas informadas não coincidem.", "erro");
            return;
        }

        /*
            PacienteRequestDTO aceita os campos abaixo.
            Os opcionais só são enviados quando preenchidos.
        */
        const corpo = {
            nome,
            email,
            senha,
            telefone: telefone || null,
            dataNascimento: dataNascimento || null,
            cpf: cpf || null,
            endereco: endereco || null
        };

        alternarCarregamento(true);

        try {
            /*
                POST /api/pacientes
                Endpoint público — permite o autocadastro.
            */
            await API.requisitar("/api/pacientes", {
                metodo: "POST",
                corpo
            });

            exibirMensagem(
                "Conta criada com sucesso! Entrando...",
                "sucesso"
            );

            /*
                Para uma experiência mais fluida, já realizamos
                o login automaticamente com as credenciais
                informadas no cadastro.
            */
            try {
                const loginResposta = await API.requisitar("/api/auth/login", {
                    metodo: "POST",
                    corpo: { email, senha }
                });

                API.salvarSessao(loginResposta);

                setTimeout(function () {
                    window.location.href = "minha-conta.html";
                }, 900);

            } catch (erroDeLogin) {
                /*
                    Se por algum motivo o login automático falhar,
                    o cadastro já foi concluído mesmo assim —
                    encaminhamos o paciente para a tela de login.
                */
                setTimeout(function () {
                    window.location.href = "login.html";
                }, 1200);
            }

        } catch (erro) {

            if (erro.status === 400) {
                /*
                    Mensagem de validação vinda do backend
                    (ex.: e-mail já cadastrado, senha curta, etc.).
                */
                exibirMensagem(erro.message, "erro");
            } else {
                exibirMensagem(erro.message, "erro");
            }

        } finally {
            alternarCarregamento(false);
        }
    });
}


function exibirMensagem(texto, tipo) {

    if (!texto) {
        mensagemCadastro.classList.add("d-none");
        mensagemCadastro.textContent = "";
        return;
    }

    mensagemCadastro.textContent = texto;
    mensagemCadastro.classList.remove("d-none");

    mensagemCadastro.classList.toggle("alerta-erro-althea", tipo === "erro");
}

function alternarCarregamento(carregando) {
    botaoCadastro.disabled = carregando;
    botaoCadastro.textContent = carregando ? "Enviando..." : "Criar conta";
}
