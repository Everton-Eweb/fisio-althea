/* ==================================================
   ESTRUTURA BASE DE INTEGRAÇÃO ENTRE FRONTEND E API
   ==================================================

   Este arquivo concentra tudo que é comum às
   requisições feitas para a API do Fisio Althea:

   - URL base da API;
   - envio automático do token JWT (quando existir);
   - leitura/gravação do token no localStorage;
   - tratamento padronizado de erros (ErroResponseDTO);
   - conversão de JSON.

   Os demais arquivos (login.js, cadastro.js, conta.js)
   utilizam as funções daqui em vez de chamar fetch()
   diretamente, evitando repetição de código.
*/

const API = (function () {

    /*
        Endereço base da API.

        Durante o desenvolvimento local a aplicação
        Spring Boot roda por padrão na porta 8080.
    */
    const BASE_URL = "http://localhost:8080";

    /*
        Chave utilizada para guardar o token
        no localStorage do navegador.
    */
    const CHAVE_TOKEN = "fisioAlthea.token";

    /*
        Chave utilizada para guardar os dados básicos
        do usuário autenticado (nome, perfil, etc.),
        devolvidos pelo /api/auth/login.
    */
    const CHAVE_USUARIO = "fisioAlthea.usuario";


    /* ==================================================
       TOKEN
       ================================================== */

    function salvarSessao(loginResponse) {
        localStorage.setItem(CHAVE_TOKEN, loginResponse.token);

        localStorage.setItem(
            CHAVE_USUARIO,
            JSON.stringify({
                idUsuario: loginResponse.idUsuario,
                nome: loginResponse.nome,
                email: loginResponse.email,
                perfil: loginResponse.perfil
            })
        );
    }

    function obterToken() {
        return localStorage.getItem(CHAVE_TOKEN);
    }

    function obterUsuario() {
        const bruto = localStorage.getItem(CHAVE_USUARIO);
        return bruto ? JSON.parse(bruto) : null;
    }

    function estaAutenticado() {
        return Boolean(obterToken());
    }

    function encerrarSessao() {
        localStorage.removeItem(CHAVE_TOKEN);
        localStorage.removeItem(CHAVE_USUARIO);
    }


    /* ==================================================
       REQUISIÇÃO PRINCIPAL
       ==================================================

       Toda chamada à API passa por aqui.

       Parâmetros:

       - caminho: ex. "/api/auth/login"
       - opcoes.metodo: "GET" (padrão), "POST", "PATCH"...
       - opcoes.corpo: objeto que será convertido em JSON
       - opcoes.autenticado: se true, envia o cabeçalho
         Authorization com o token salvo
    */
    async function requisitar(caminho, opcoes = {}) {

        const {
            metodo = "GET",
            corpo = null,
            autenticado = false
        } = opcoes;

        const cabecalhos = {
            "Content-Type": "application/json"
        };

        if (autenticado) {
            const token = obterToken();

            if (!token) {
                throw new ErroApi(
                    "Sessão expirada. Faça login novamente.",
                    401
                );
            }

            cabecalhos["Authorization"] = "Bearer " + token;
        }

        let resposta;

        try {
            resposta = await fetch(BASE_URL + caminho, {
                method: metodo,
                headers: cabecalhos,
                body: corpo ? JSON.stringify(corpo) : undefined
            });
        } catch (erroDeRede) {
            /*
                Ocorre quando a API está fora do ar
                ou existe algum problema de conexão/CORS.
            */
            throw new ErroApi(
                "Não foi possível conectar ao servidor. Tente novamente em instantes.",
                0
            );
        }

        /*
            HTTP 204 (No Content) não possui corpo
            para ser convertido em JSON.
        */
        if (resposta.status === 204) {
            return null;
        }

        const dados = await resposta.json().catch(() => null);

        if (!resposta.ok) {
            /*
                O backend padroniza os erros através
                do ErroResponseDTO: { status, erro, mensagem }.
            */
            const mensagem =
                (dados && dados.mensagem) ||
                "Ocorreu um erro inesperado. Tente novamente.";

            throw new ErroApi(mensagem, resposta.status);
        }

        return dados;
    }


    /* ==================================================
       API PÚBLICA DO MÓDULO
       ================================================== */

    return {
        requisitar,
        salvarSessao,
        obterToken,
        obterUsuario,
        estaAutenticado,
        encerrarSessao
    };

})();


/*
    Classe de erro personalizada para diferenciar
    falhas da API de outros erros de programação.

    Guarda também o status HTTP, o que permite
    tratamentos diferentes conforme o caso
    (401, 400, 404, etc.) quando necessário.
*/
class ErroApi extends Error {
    constructor(mensagem, status) {
        super(mensagem);
        this.name = "ErroApi";
        this.status = status;
    }
}
