/* ==================================================
   FORMULÁRIO DE CADASTRO DE PACIENTE
   ==================================================

   Responsável por:
   - capturar os dados informados no formulário;
   - realizar validações básicas no frontend;
   - enviar o cadastro para a API;
   - tratar mensagens de sucesso e erro;
   - direcionar o paciente para o login após o cadastro.

   Tópicos correspondentes:
   - Desenvolvimento Web
   - APIs REST
   - Arquitetura de Software
   - Levantamento e Especificação de Requisitos
   ================================================== */

const formularioCadastro = document.querySelector("#formCadastro");
const mensagemCadastro = document.querySelector("#mensagemCadastro");
const botaoCadastro = document.querySelector("#botaoCadastro");


/* ==================================================
   VERIFICAÇÃO DE SESSÃO
   ==================================================

   Caso o usuário já esteja autenticado, não há
   necessidade de acessar novamente a tela de cadastro.
*/
if (API.estaAutenticado()) {
    window.location.href = "minha-conta.html";
}


/* ==================================================
   ENVIO DO FORMULÁRIO
   ================================================== */

if (formularioCadastro) {

    formularioCadastro.addEventListener("submit", async function (evento) {

        /*
            Impede o comportamento padrão do formulário,
            evitando o recarregamento da página.
        */
        evento.preventDefault();

        /*
            Remove mensagens anteriores antes de uma
            nova tentativa de cadastro.
        */
        exibirMensagem(null);

        /*
            Recupera os valores informados pelo usuário.
        */
        const nome = document.querySelector("#nome").value.trim();
        const email = document.querySelector("#email").value.trim();
        const senha = document.querySelector("#senha").value;
        const confirmarSenha = document.querySelector("#confirmarSenha").value;
        const telefone = document.querySelector("#telefone").value.trim();
        const dataNascimento = document.querySelector("#dataNascimento").value;
        const cpf = document.querySelector("#cpf").value.trim();
        const endereco = document.querySelector("#endereco").value.trim();


        /* ==================================================
           VALIDAÇÕES DO FRONTEND
           ==================================================

           Estas validações melhoram a experiência do usuário.

           O backend continua sendo responsável pela
           validação definitiva dos dados.
        */

        if (!nome || !email || !senha) {
            exibirMensagem(
                "Preencha nome, e-mail e senha para continuar.",
                "erro"
            );
            return;
        }

        if (senha.length < 6) {
            exibirMensagem(
                "A senha deve possuir pelo menos 6 caracteres.",
                "erro"
            );
            return;
        }

        if (senha !== confirmarSenha) {
            exibirMensagem(
                "As senhas informadas não coincidem.",
                "erro"
            );
            return;
        }


        /* ==================================================
           MONTAGEM DO OBJETO ENVIADO À API
           ==================================================

           PacienteRequestDTO aceita os campos abaixo.

           Os campos opcionais são enviados como null
           quando não forem preenchidos.

           Campos administrativos como perfil, ativo,
           idUsuario e idPaciente não são enviados pelo
           formulário público de cadastro.
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


        /*
            Desabilita temporariamente o botão para evitar
            múltiplos envios enquanto a API responde.
        */
        alternarCarregamento(true);


        try {

            /*
                POST /api/pacientes

                Como o BASE_URL do módulo API já contém
                "/api", aqui informamos somente "/pacientes".

                Este endpoint é público e permite o
                autocadastro do paciente.
            */
            await API.requisitar("/pacientes", {
                metodo: "POST",
                corpo
            });


            /* ==================================================
               CADASTRO REALIZADO
               ==================================================

               O cadastro não realiza login automático.

               Após criar a conta, o paciente é enviado
               para a tela de login, onde realizará a
               autenticação e receberá seu token JWT.
            */

            exibirMensagem(
                "Conta criada com sucesso! Você será direcionado para o login.",
                "sucesso"
            );

            setTimeout(function () {
                window.location.href = "login.html";
            }, 1500);


        } catch (erro) {

            /*
                API.requisitar() transforma respostas de erro
                da API em ErroApi.

                A mensagem enviada pelo backend é exibida
                ao usuário quando estiver disponível.
            */
            exibirMensagem(
                erro.message || "Não foi possível criar a conta. Tente novamente.",
                "erro"
            );


        } finally {

            /*
                Reativa o botão independentemente do resultado
                da requisição.
            */
            alternarCarregamento(false);
        }
    });
}


/* ==================================================
   EXIBIÇÃO DE MENSAGENS
   ================================================== */

function exibirMensagem(texto, tipo) {

    if (!mensagemCadastro) {
        return;
    }

    /*
        Quando nenhum texto é informado, escondemos
        a área de mensagens.
    */
    if (!texto) {
        mensagemCadastro.classList.add("d-none");
        mensagemCadastro.textContent = "";
        mensagemCadastro.classList.remove("alerta-erro-althea");
        return;
    }

    mensagemCadastro.textContent = texto;
    mensagemCadastro.classList.remove("d-none");

    /*
        A classe de erro é aplicada somente quando
        a mensagem representar uma falha.
    */
    mensagemCadastro.classList.toggle(
        "alerta-erro-althea",
        tipo === "erro"
    );
}


/* ==================================================
   ESTADO DE CARREGAMENTO
   ================================================== */

function alternarCarregamento(carregando) {

    if (!botaoCadastro) {
        return;
    }

    botaoCadastro.disabled = carregando;

    botaoCadastro.textContent =
        carregando ? "Enviando..." : "Criar conta";
}