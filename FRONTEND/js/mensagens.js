// ======================================================
// FISIO ALTHEA - ÁREA DO PACIENTE
// Issue #5 - Mensagens e tratamento de erros
// ======================================================

document.addEventListener("DOMContentLoaded", iniciarAreaPaciente);

let pacienteAtual = null;
let mensagemAtual = null;

function iniciarAreaPaciente() {
    if (!API.estaAutenticado()) {
        window.location.href = "login.html?redirecionado=1";
        return;
    }

    document.getElementById("botaoSair")
        .addEventListener("click", sair);

    document.getElementById("formMensagem")
        .addEventListener("submit", enviarMensagem);

    document.getElementById("botaoAtualizar")
        .addEventListener("click", carregarMensagens);

    document.getElementById("botaoFecharDetalhes")
        .addEventListener("click", fecharDetalhes);

    document.getElementById("botaoMarcarLida")
        .addEventListener("click", marcarComoLida);

    inicializar();
}

async function inicializar() {
    try {
        pacienteAtual = await API.requisitar("/pacientes/me", {
            metodo: "GET",
            autenticado: true
        });

        if (!pacienteAtual || pacienteAtual.idPaciente == null) {
            mostrarMensagemGeral(
                "Não foi possível identificar o cadastro do paciente.",
                "danger"
            );
            return;
        }

        await carregarMensagens();
    } catch (erro) {
        mostrarErroGeral(erro);
    }
}

// ======================================================
// ENVIO DE MENSAGEM
// ======================================================

async function enviarMensagem(evento) {
    evento.preventDefault();

    if (!pacienteAtual || pacienteAtual.idPaciente == null) {
        mostrarMensagemGeral(
            "Os dados do paciente ainda não foram carregados.",
            "warning"
        );
        return;
    }

    const formulario = document.getElementById("formMensagem");
    const botao = document.getElementById("botaoEnviar");

    const assunto = document.getElementById("assunto").value.trim();
    const conteudo = document.getElementById("conteudo").value.trim();

    if (!conteudo) {
        mostrarMensagemGeral(
            "Digite o conteúdo da mensagem antes de enviar.",
            "warning"
        );
        return;
    }

    botao.disabled = true;
    botao.textContent = "Enviando...";

    try {
        await API.requisitar("/mensagens", {
            metodo: "POST",
            autenticado: true,
            corpo: {
                idPaciente: pacienteAtual.idPaciente,
                assunto: assunto || null,
                conteudo: conteudo
            }
        });

        formulario.reset();

        mostrarMensagemGeral(
            "Mensagem enviada com sucesso!",
            "success"
        );

        fecharDetalhes();
        await carregarMensagens();
    } catch (erro) {
        mostrarErroGeral(erro);
    } finally {
        botao.disabled = false;
        botao.textContent = "Enviar mensagem";
    }
}

// ======================================================
// LISTAGEM
// ======================================================

async function carregarMensagens() {
    const lista = document.getElementById("listaMensagens");
    const aviso = document.getElementById("mensagemLista");

    lista.replaceChildren();

    mostrarAvisoLista("Carregando mensagens...", "info");

    try {
        const mensagens = await API.requisitar("/mensagens/me", {
            metodo: "GET",
            autenticado: true
        });

        aviso.classList.add("d-none");

        if (!Array.isArray(mensagens) || mensagens.length === 0) {
            mostrarAvisoLista(
                "Você ainda não possui mensagens.",
                "secondary"
            );
            return;
        }

        mensagens.sort((a, b) => {
            return new Date(b.dataEnvio) - new Date(a.dataEnvio);
        });

        mensagens.forEach(criarCardMensagem);
    } catch (erro) {
        mostrarAvisoLista(erro.message, "danger");
    }
}

function criarCardMensagem(mensagem) {
    const lista = document.getElementById("listaMensagens");

    const coluna = document.createElement("div");
    coluna.className = "col-12";

    const card = document.createElement("article");
    card.className = "card shadow-sm";

    const corpo = document.createElement("div");
    corpo.className = "card-body";

    const linhaTopo = document.createElement("div");
    linhaTopo.className =
        "d-flex flex-wrap justify-content-between align-items-start gap-2";

    const titulo = document.createElement("h3");
    titulo.className = "h5 mb-2";
    titulo.textContent = mensagem.assunto || "Mensagem sem assunto";

    const estado = document.createElement("span");
    estado.className = mensagem.lida
        ? "badge text-bg-secondary"
        : "badge text-bg-primary";

    estado.textContent = mensagem.lida ? "Lida" : "Não lida";

    const data = document.createElement("p");
    data.className = "text-muted small mb-2";
    data.textContent = formatarData(mensagem.dataEnvio);

    const remetente = document.createElement("p");
    remetente.className = "mb-3";
    remetente.textContent =
        "Usuário: " + (mensagem.nomeUsuario || "Não informado");

    const botao = document.createElement("button");
    botao.type = "button";
    botao.className = "btn btn-outline-primary btn-sm";
    botao.textContent = "Visualizar mensagem";

    botao.addEventListener("click", () => {
        abrirMensagem(mensagem.idMensagem);
    });

    linhaTopo.append(titulo, estado);
    corpo.append(linhaTopo, data, remetente, botao);
    card.appendChild(corpo);
    coluna.appendChild(card);
    lista.appendChild(coluna);
}

// ======================================================
// VISUALIZAÇÃO
// ======================================================

async function abrirMensagem(idMensagem) {
    if (idMensagem == null) {
        mostrarMensagemGeral(
            "Não foi possível identificar a mensagem selecionada.",
            "danger"
        );
        return;
    }

    try {
        const mensagem = await API.requisitar(
            "/mensagens/" + encodeURIComponent(idMensagem),
            {
                metodo: "GET",
                autenticado: true
            }
        );

        mensagemAtual = mensagem;

        document.getElementById("detalheAssunto").textContent =
            mensagem.assunto || "Mensagem sem assunto";

        document.getElementById("detalheUsuario").textContent =
            mensagem.nomeUsuario || "Não informado";

        document.getElementById("detalheData").textContent =
            formatarData(mensagem.dataEnvio);

        document.getElementById("detalheConteudo").textContent =
            mensagem.conteudo || "";

        atualizarEstadoDetalhes(mensagem);

        document.getElementById("secaoDetalhes").classList.remove("d-none");

        document.getElementById("secaoDetalhes")
            .scrollIntoView({ behavior: "smooth", block: "start" });

    } catch (erro) {
        mostrarMensagemGeral(erro.message, "danger");
    }
}

function atualizarEstadoDetalhes(mensagem) {
    const estado = document.getElementById("detalheEstado");
    const botao = document.getElementById("botaoMarcarLida");

    estado.textContent = mensagem.lida
        ? "Esta mensagem está marcada como lida."
        : "Esta mensagem ainda não foi lida.";

    botao.classList.toggle("d-none", Boolean(mensagem.lida));
}

function fecharDetalhes() {
    mensagemAtual = null;

    document.getElementById("secaoDetalhes").classList.add("d-none");
}

// ======================================================
// MARCAR COMO LIDA
// ======================================================

async function marcarComoLida() {
    if (!mensagemAtual || mensagemAtual.idMensagem == null) {
        return;
    }

    const botao = document.getElementById("botaoMarcarLida");

    botao.disabled = true;
    botao.textContent = "Atualizando...";

    try {
        const atualizada = await API.requisitar(
            "/mensagens/" +
            encodeURIComponent(mensagemAtual.idMensagem) +
            "/lida",
            {
                metodo: "PATCH",
                autenticado: true
            }
        );

        mensagemAtual = atualizada || {
            ...mensagemAtual,
            lida: true
        };

        atualizarEstadoDetalhes(mensagemAtual);

        mostrarMensagemGeral(
            "Mensagem marcada como lida.",
            "success"
        );

        await carregarMensagens();
    } catch (erro) {
        mostrarMensagemGeral(erro.message, "danger");
    } finally {
        botao.disabled = false;
        botao.textContent = "Marcar como lida";
    }
}

// ======================================================
// MENSAGENS E ERROS DE INTERFACE
// ======================================================

function mostrarMensagemGeral(texto, tipo) {
    const elemento = document.getElementById("mensagemGeral");

    elemento.textContent = texto;
    elemento.className = "alert alert-" + tipo;
    elemento.classList.remove("d-none");
}

function mostrarErroGeral(erro) {
    mostrarMensagemGeral(
        erro && erro.message
            ? erro.message
            : "Ocorreu um erro inesperado. Tente novamente.",
        "danger"
    );
}

function mostrarAvisoLista(texto, tipo) {
    const elemento = document.getElementById("mensagemLista");

    elemento.textContent = texto;
    elemento.className = "alert alert-" + tipo;
    elemento.classList.remove("d-none");
}

function formatarData(valor) {
    if (!valor) {
        return "Data não informada";
    }

    const data = new Date(valor);

    if (Number.isNaN(data.getTime())) {
        return "Data não disponível";
    }

    return data.toLocaleString("pt-BR");
}

function sair() {
    API.encerrarSessao();
    window.location.href = "login.html";
}