/* ==================================================
   DISPONIBILIDADE E AGENDAMENTOS — Issue #4
   ==================================================

   Endpoints (contrato conferido nos Controllers/DTOs do backend):

   GET   /api/disponibilidades/horarios?idProfissional=&idServico=&data=AAAA-MM-DD
         -> { idProfissional, idServico, data, horarios: ["08:00:00", ...] }

   POST  /api/agendamentos
         corpo: { idPaciente, idProfissional, idServico, data, hora }

   GET   /api/agendamentos/me
         -> [ { idAgendamento, idProfissional, nomeProfissional,
                idServico, nomeServico, data, hora, status, ... } ]

   PATCH /api/agendamentos/{id}/cancelar      (sem corpo)
   PATCH /api/agendamentos/{id}/reagendar     corpo: { data, hora }

   Toda requisição passa por API.requisitar (api.js).
   Disponibilidade, conflito e demais regras ficam no backend:
   aqui só exibimos o que a API devolve.
*/

const mensagemAgendamento = document.getElementById("mensagemAgendamento");
const formAgendarHorario = document.getElementById("formAgendarHorario");
const selectProfissional = document.getElementById("selectProfissional");
const selectServico = document.getElementById("selectServico");
const campoData = document.getElementById("campoData");
const listaHorarios = document.getElementById("listaHorarios");
const botaoConfirmar = document.getElementById("botaoConfirmar");
const botaoCancelarEdicao = document.getElementById("botaoCancelarEdicao");
const tituloFormulario = document.getElementById("tituloFormulario");
const listaMeusAgendamentos = document.getElementById("listaMeusAgendamentos");

let idPaciente = null;          // vem de GET /api/pacientes/me
let horarioEscolhido = null;    // "HH:mm:ss", exatamente como a API devolveu
let idReagendando = null;       // preenchido só no modo reagendar
let numeroConsulta = 0;         // ignora respostas antigas de horários
let enviandoAgendamento = false; // Impede envios simultâneos
let cancelamentosEmAndamento = new Set(); // Controla cancelamentos por ID

if (!API.estaAutenticado()) {
    window.location.href = "login.html?redirecionado=1";
} else {
    iniciar();
}


/* ==================================================
   INICIALIZAÇÃO
   ================================================== */

async function iniciar() {

    selectProfissional.addEventListener("change", aoMudarFiltro);
    selectServico.addEventListener("change", aoMudarFiltro);
    campoData.addEventListener("change", aoMudarFiltro);
    formAgendarHorario.addEventListener("submit", enviarFormulario);
    botaoCancelarEdicao.addEventListener("click", sairDoModoReagendar);

    await Promise.all([
        carregarOpcoes(),
        carregarPaciente(),
        carregarMeusAgendamentos()
    ]);
}

async function carregarOpcoes() {
    try {
        const [profissionais, servicos] = await Promise.all([
            API.requisitar("/profissionais"),
            API.requisitar("/servicos")
        ]);

        (profissionais || []).forEach(function (p) {
            selectProfissional.appendChild(
                new Option(p.nome, p.idProfissional)
            );
        });

        /*
            Todos os serviços retornados são listados.
            Se o backend recusar um serviço, a mensagem da API é exibida.
        */
        (servicos || []).forEach(function (s) {
            const texto = s.ativo ? s.nome : s.nome + " (indisponível)";
            selectServico.appendChild(new Option(texto, s.idServico));
        });

    } catch (erro) {
        tratarErro(erro);
    }
}

/*
    O POST exige idPaciente, e o paciente só pode agendar para si.
    O id vem de GET /api/pacientes/me.
*/
async function carregarPaciente() {
    try {
        const paciente = await API.requisitar("/pacientes/me", {
            autenticado: true
        });
        idPaciente = paciente.idPaciente;
    } catch (erro) {
        tratarErro(erro);
    }
}


/* ==================================================
   HORÁRIOS DISPONÍVEIS
   ================================================== */

function aoMudarFiltro() {
    exibirMensagem(null);
    carregarHorarios();
}

async function carregarHorarios() {

    const consulta = ++numeroConsulta;

    horarioEscolhido = null;
    botaoConfirmar.disabled = true;
    listaHorarios.innerHTML = "";

    const idProfissional = selectProfissional.value;
    const idServico = selectServico.value;
    const data = campoData.value;

    if (!idProfissional || !idServico || !data) {
        return;
    }

    const parametros = new URLSearchParams({ idProfissional, idServico, data });

    try {
        const resposta = await API.requisitar(
            "/disponibilidades/horarios?" + parametros.toString()
        );

        if (consulta !== numeroConsulta) {
            return; /* o usuário já mudou a seleção */
        }

        const horarios = (resposta && resposta.horarios) || [];

        if (horarios.length === 0) {
            const aviso = document.createElement("p");
            aviso.className = "mb-0";
            aviso.textContent =
                "Não há horários disponíveis nesta data. Escolha outro dia, serviço ou profissional.";
            listaHorarios.appendChild(aviso);
            return;
        }

        /* Somente o que a API devolveu é exibido. */
        horarios.forEach(function (hora) {
            const botao = document.createElement("button");
            botao.type = "button";
            botao.className = "btn btn-outline-secondary me-2 mb-2";
            botao.textContent = hora.slice(0, 5);

            botao.addEventListener("click", function () {

                if (enviandoAgendamento) {
                    return;
                }

                listaHorarios
                    .querySelectorAll("button")
                    .forEach(function (b) { b.classList.remove("active"); });

                botao.classList.add("active");
                horarioEscolhido = hora;
                botaoConfirmar.disabled = false;
            });

            listaHorarios.appendChild(botao);
        });

    } catch (erro) {
        if (consulta === numeroConsulta) {
            tratarErro(erro);
        }
    }
}


/* ==================================================
   CRIAR OU REAGENDAR
   ================================================== */

async function enviarFormulario(evento) {
    evento.preventDefault();

    // Impede que outra solicitação seja enviada enquanto a atual está em andamento.
    if (enviandoAgendamento) {
        return;
    }

    if (!horarioEscolhido) {
        exibirMensagem("Escolha um horário para continuar.", "erro");
        return;
    }

    enviandoAgendamento = true;

    // Invalida consultas de disponibilidade iniciadas antes do envio.
    numeroConsulta++;

    botaoConfirmar.disabled = true;
    controlarCamposDuranteEnvio(true);

    try {
        if (idReagendando) {
            /* PATCH /api/agendamentos/{id}/reagendar */
            await API.requisitar(`/agendamentos/${idReagendando}/reagendar`, {
                metodo: "PATCH",
                autenticado: true,
                corpo: { data: campoData.value, hora: horarioEscolhido }
            });

            sairDoModoReagendar();
            exibirMensagem("Agendamento reagendado com sucesso.", "sucesso");

        } else {

            if (idPaciente == null) {
                exibirMensagem(
                    "Não foi possível identificar o paciente. Entre novamente.",
                    "erro"
                );
                botaoConfirmar.disabled = false;
                return;
            }

            /* POST /api/agendamentos */
            await API.requisitar("/agendamentos", {
                metodo: "POST",
                autenticado: true,
                corpo: {
                    idPaciente: idPaciente,
                    idProfissional: Number(selectProfissional.value),
                    idServico: Number(selectServico.value),
                    data: campoData.value,
                    hora: horarioEscolhido
                }
            });

            formAgendarHorario.reset();
            exibirMensagem("Agendamento criado com sucesso.", "sucesso");
        }

        listaHorarios.innerHTML = "";
        horarioEscolhido = null;
        await carregarMeusAgendamentos();

    } catch (erro) {
        tratarErro(erro);

        // Consulta novamente a disponibilidade após a falha.
        // O botão permanecerá desabilitado até uma nova seleção.
        await carregarHorarios();

    } finally {
        enviandoAgendamento = false;
        controlarCamposDuranteEnvio(false);
    }
}

function controlarCamposDuranteEnvio(enviando) {
    selectProfissional.disabled = enviando || idReagendando !== null;
    selectServico.disabled = enviando || idReagendando !== null;
    campoData.disabled = enviando;
    botaoCancelarEdicao.disabled = enviando;
}


/* ==================================================
   MEUS AGENDAMENTOS
   ================================================== */

async function carregarMeusAgendamentos() {

    try {
        const agendamentos = await API.requisitar("/agendamentos/me", {
            autenticado: true
        });

        listaMeusAgendamentos.innerHTML = "";

        if (!agendamentos || agendamentos.length === 0) {
            const vazio = document.createElement("p");
            vazio.className = "col-12 text-center mb-0";
            vazio.textContent = "Você ainda não tem agendamentos.";
            listaMeusAgendamentos.appendChild(vazio);
            return;
        }

        agendamentos.forEach(function (ag) {
            listaMeusAgendamentos.appendChild(criarCardAgendamento(ag));
        });

    } catch (erro) {
        tratarErro(erro);
    }
}

const ROTULO_STATUS = {
    AGENDADO: "Agendado",
    CONFIRMADO: "Confirmado",
    CANCELADO: "Cancelado"
};

function criarCardAgendamento(ag) {

    const coluna = document.createElement("div");
    coluna.className = "col-12 col-md-6 col-lg-4";

    const card = document.createElement("div");
    card.className = "formulario-agendamento h-100";

    const titulo = document.createElement("h3");
    titulo.className = "h5 mb-3";
    titulo.textContent = ag.nomeServico || "Atendimento";

    const profissional = document.createElement("p");
    profissional.className = "mb-2";
    profissional.textContent = "Profissional: " + (ag.nomeProfissional || "—");

    const quando = document.createElement("p");
    quando.className = "mb-2";
    quando.textContent =
        "Data: " + formatarData(ag.data) + " às " + (ag.hora || "").slice(0, 5);

    const situacao = document.createElement("p");
    situacao.className = "mb-2";
    situacao.textContent =
        "Situação: " + (ROTULO_STATUS[ag.status] || ag.status || "—");

    card.append(titulo, profissional, quando, situacao);

    if (ag.status !== "CANCELADO") {

        const reagendar = document.createElement("button");
        reagendar.type = "button";
        reagendar.className = "btn btn-outline-secondary me-2 mt-2";
        reagendar.textContent = "Reagendar";
        reagendar.addEventListener("click", function () {
            entrarNoModoReagendar(ag);
        });

        const cancelar = document.createElement("button");
        cancelar.type = "button";
        cancelar.className = "btn btn-outline-danger mt-2";
        cancelar.textContent = "Cancelar";
        cancelar.addEventListener("click", function () {
            cancelarAgendamento(ag.idAgendamento);
        });

        card.append(reagendar, cancelar);
    }

    coluna.appendChild(card);
    return coluna;
}

async function cancelarAgendamento(id) {

    // Impede cancelamentos durante o envio de outro agendamento.
    if (enviandoAgendamento) {
        return;
    }

    // Impede duas solicitações simultâneas para o mesmo agendamento.
    if (cancelamentosEmAndamento.has(id)) {
        return;
    }

    if (!window.confirm("Deseja realmente cancelar este agendamento?")) {
        return;
    }

    cancelamentosEmAndamento.add(id);

    try {
        await API.requisitar(`/agendamentos/${id}/cancelar`, {
            metodo: "PATCH",
            autenticado: true
        });

        // Se o atendimento cancelado estava sendo reagendado,
        // fecha o modo de edição e limpa o formulário.
        if (idReagendando === id) {
            sairDoModoReagendar();
        }

        exibirMensagem("Agendamento cancelado.", "sucesso");
        await carregarMeusAgendamentos();

    } catch (erro) {
        tratarErro(erro);

    } finally {
        cancelamentosEmAndamento.delete(id);
    }
}

/* ==================================================
   MODO REAGENDAR (reaproveita o formulário e os horários)
   ================================================== */

function entrarNoModoReagendar(ag) {

    if (
        enviandoAgendamento ||
        cancelamentosEmAndamento.has(ag.idAgendamento)
    ) {
        return;
    }

    numeroConsulta++; // Invalida consultas anteriores de horários

    idReagendando = ag.idAgendamento;

    tituloFormulario.textContent = "Reagendar atendimento";
    botaoConfirmar.textContent = "Confirmar novo horário";
    botaoCancelarEdicao.classList.remove("d-none");

    /* Profissional e serviço continuam os do agendamento original. */
    selectProfissional.value = ag.idProfissional;
    selectServico.value = ag.idServico;
    selectProfissional.disabled = true;
    selectServico.disabled = true;

    campoData.value = "";
    listaHorarios.innerHTML = "";
    horarioEscolhido = null;
    botaoConfirmar.disabled = true;
    exibirMensagem(null);

    formAgendarHorario.scrollIntoView({ behavior: "smooth" });
}

function sairDoModoReagendar() {

    numeroConsulta++; // Invalida consultas anteriores de horários

    idReagendando = null;

    tituloFormulario.textContent = "Novo agendamento";
    botaoConfirmar.textContent = "Confirmar agendamento";
    botaoCancelarEdicao.classList.add("d-none");

    // Respeita o bloqueio enquanto a requisição ainda está em andamento.
    controlarCamposDuranteEnvio(enviandoAgendamento);

    formAgendarHorario.reset();
    listaHorarios.innerHTML = "";
    horarioEscolhido = null;
    botaoConfirmar.disabled = true;
}


/* ==================================================
   AUXILIARES
   ================================================== */

/* "2026-09-02" -> "02/09/2026" (sem usar Date, para não deslocar o dia) */
function formatarData(dataIso) {
    if (!dataIso) return "—";
    const [ano, mes, dia] = dataIso.split("-");
    return `${dia}/${mes}/${ano}`;
}

/* 401: sessão inválida. 403: perfil sem acesso. Demais: mensagem da API. */
function tratarErro(erro) {

    if (erro.status === 401) {
        API.encerrarSessao();
        window.location.href = "login.html?expirado=1";
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

function exibirMensagem(texto, tipo) {

    if (!texto) {
        mensagemAgendamento.classList.add("d-none");
        mensagemAgendamento.textContent = "";
        return;
    }

    mensagemAgendamento.textContent = texto;
    mensagemAgendamento.classList.remove("d-none");
    mensagemAgendamento.classList.toggle("alerta-erro-althea", tipo === "erro");
    mensagemAgendamento.scrollIntoView({ behavior: "smooth", block: "nearest" });
}
