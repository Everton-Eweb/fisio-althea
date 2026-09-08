/*
 * ==================================================
 * CONFIGURAÇÃO CENTRAL DA API - FISIO ALTHEA
 * ==================================================
 *
 * Este arquivo concentra as configurações utilizadas
 * pelo frontend para comunicação com a API REST
 * desenvolvida em Spring Boot.
 *
 * Dessa forma, os demais arquivos JavaScript não
 * precisarão repetir o endereço do backend.
 *
 * Tópicos correspondentes:
 * - Desenvolvimento Web
 * - APIs REST
 * - Arquitetura de Software
 */

const API_BASE_URL = "http://localhost:8080/api";

/*
 * ==================================================
 * REQUISIÇÃO PADRÃO PARA A API
 * ==================================================
 *
 * Centraliza as chamadas feitas ao backend.
 *
 * Assim, os outros módulos do frontend não precisam
 * repetir a configuração do endereço da API.
 *
 * Quando existir um token JWT salvo, ele também será
 * enviado automaticamente no cabeçalho Authorization.
 *
 * Tópicos correspondentes:
 * - Desenvolvimento Web
 * - APIs REST
 * - Arquitetura de Software
 */
async function requisicaoApi(endpoint, opcoes = {}) {

    /*
     * Recupera o JWT armazenado no navegador.
     *
     * O módulo de autenticação será responsável
     * posteriormente por salvar esse token.
     */
    const token = localStorage.getItem("fisioAltheaToken");

    /*
     * Cria os cabeçalhos da requisição preservando
     * aqueles enviados pelo módulo que chamou a função.
     */
    const headers = new Headers(opcoes.headers || {});

    /*
     * Caso exista corpo na requisição, consideramos
     * que os dados serão enviados em JSON.
     */
    if (opcoes.body && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }

    /*
     * Se houver um JWT armazenado, adicionamos
     * automaticamente:
     *
     * Authorization: Bearer <token>
     */
    if (token) {
        headers.set(
            "Authorization",
            `Bearer ${token}`
        );
    }

    /*
     * Realiza a chamada HTTP utilizando a URL
     * central configurada no início deste arquivo.
     */
    return fetch(
        `${API_BASE_URL}${endpoint}`,
        {
            ...opcoes,
            headers: headers
        }
    );
}