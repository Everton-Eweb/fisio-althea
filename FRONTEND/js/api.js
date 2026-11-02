
// ======================================================
// FISIO ALTHEA - INTEGRAÇÃO COM A API
// Tratamento centralizado de erros
// ======================================================

const API = (function () {
    const BASE_URL = "http://localhost:8080/api";

    const CHAVE_TOKEN = "fisioAltheaToken";
    const CHAVE_USUARIO = "fisioAlthea.usuario";

    // ==================================================
    // SESSÃO
    // ==================================================

    function salvarSessao(loginResponse) {
        if (!loginResponse || !loginResponse.token) {
            throw new Error(
                "A resposta do login não contém um token válido."
            );
        }

        const usuario = {
            idUsuario: loginResponse.idUsuario,
            nome: loginResponse.nome,
            email: loginResponse.email,
            perfil: loginResponse.perfil
        };

        localStorage.setItem(CHAVE_TOKEN, loginResponse.token);
        localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario));
    }

    function obterToken() {
        return localStorage.getItem(CHAVE_TOKEN);
    }

    function obterUsuario() {
        const usuario = localStorage.getItem(CHAVE_USUARIO);

        if (!usuario) {
            return null;
        }

        try {
            return JSON.parse(usuario);
        } catch (erro) {
            console.error(
                "Não foi possível ler os dados do usuário:",
                erro
            );
            return null;
        }
    }

    function estaAutenticado() {
        return Boolean(obterToken());
    }

    function encerrarSessao() {
        localStorage.removeItem(CHAVE_TOKEN);
        localStorage.removeItem(CHAVE_USUARIO);
    }

    // ==================================================
    // MENSAGENS PADRÃO DE ERRO
    // ==================================================

    function obterMensagemPadrao(status) {
        switch (status) {
            case 400:
                return "Os dados enviados são inválidos. Confira as informações e tente novamente.";

            case 401:
                return "Sua sessão expirou. Entre novamente para continuar.";

            case 403:
                return "Você não tem permissão para realizar esta ação.";

            case 404:
                return "O recurso solicitado não foi encontrado.";

            case 409:
                return "Esta operação não pode ser concluída porque existe um conflito com os dados.";

            case 422:
                return "Não foi possível processar os dados informados. Confira as informações.";

            default:
                if (status >= 500) {
                    return "Ocorreu um erro interno no servidor. Tente novamente mais tarde.";
                }

                return "Não foi possível concluir a solicitação. Tente novamente.";
        }
    }

    // ==================================================
    // EXTRAÇÃO DA MENSAGEM DO BACKEND
    // ==================================================

    function extrairMensagem(dados, status) {
        if (dados && typeof dados === "object") {
            const mensagem =
                dados.mensagem ||
                dados.message ||
                dados.detail ||
                dados.erro;

            if (
                typeof mensagem === "string" &&
                mensagem.trim()
            ) {
                return mensagem;
            }
        }

        // Não exibe respostas HTML ou textos técnicos
        // retornados pelo servidor.
        return obterMensagemPadrao(status);
    }

    // ==================================================
    // TRATAMENTO DE SESSÃO EXPIRADA
    // ==================================================

    function tratarSessaoExpirada() {
        encerrarSessao();

        const paginaAtual = window.location.pathname;

        const paginaLogin = paginaAtual
            .toLowerCase()
            .endsWith("/login.html");

        if (!paginaLogin) {
            window.location.href = "login.html?expirado=1";
        }
    }

    // ==================================================
    // REQUISIÇÕES HTTP
    // ==================================================

    async function requisitar(caminho, opcoes = {}) {
        const {
            metodo = "GET",
            corpo = null,
            autenticado = false
        } = opcoes;

        const cabecalhos = {
            Accept: "application/json"
        };

        const temCorpo =
            corpo !== null &&
            corpo !== undefined;

        if (temCorpo) {
            cabecalhos["Content-Type"] = "application/json";
        }

        const token = obterToken();

        // Requisição protegida sem token disponível.
        if (autenticado && !token) {
            tratarSessaoExpirada();

            throw new ErroApi(
                "Sua sessão expirou. Entre novamente para continuar.",
                401
            );
        }

        if (token) {
            cabecalhos.Authorization = `Bearer ${token}`;
        }

        const configuracao = {
            method: metodo,
            headers: cabecalhos
        };

        if (temCorpo) {
            configuracao.body = JSON.stringify(corpo);
        }

        let resposta;

        try {
            resposta = await fetch(
                `${BASE_URL}${caminho}`,
                configuracao
            );
        } catch (erro) {
            console.error(
                "Erro de conexão com a API:",
                erro
            );

            throw new ErroApi(
                "Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.",
                0
            );
        }

        // Resposta sem conteúdo.
        if (resposta.status === 204) {
            return null;
        }

        // Leitura segura do conteúdo retornado pela API.
        const texto = await resposta.text();

        let dados = null;

        if (texto) {
            try {
                dados = JSON.parse(texto);
            } catch (erro) {
                // A resposta não é JSON válido.
                dados = null;
            }
        }

        // ==================================================
        // TRATAMENTO CENTRALIZADO DE ERROS HTTP
        // ==================================================

        if (!resposta.ok) {
            const mensagem = extrairMensagem(
                dados,
                resposta.status
            );

            console.error("Erro retornado pela API:", {
                status: resposta.status,
                mensagem: mensagem
            });

            // Encerra a sessão somente quando uma
            // requisição autenticada retorna HTTP 401.
            if (
                resposta.status === 401 &&
                autenticado
            ) {
                tratarSessaoExpirada();
            }

            throw new ErroApi(
                mensagem,
                resposta.status
            );
        }

        return dados;
    }

    // ==================================================
    // FUNÇÕES DISPONÍVEIS PARA O RESTANTE DO SISTEMA
    // ==================================================

    return {
        requisitar,
        salvarSessao,
        obterToken,
        obterUsuario,
        estaAutenticado,
        encerrarSessao
    };
})();

// ======================================================
// ERRO PERSONALIZADO DA API
// ======================================================

class ErroApi extends Error {
    constructor(mensagem, status = 0) {
        super(mensagem);

        this.name = "ErroApi";
        this.status = status;
    }
}
