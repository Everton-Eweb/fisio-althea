/* ==================================================
   CONTROLE DE ACESSO — FISIO ALTHEA
   ==================================================

   Responsável por verificar se o usuário possui
   uma sessão local e se seu perfil corresponde
   ao exigido pela página.

   Áreas curriculares:
   - Desenvolvimento Web
   - Segurança de Aplicações
   - Arquitetura de Software
*/

const ControleAcesso = (function () {

    /*
     * Verifica a sessão e o perfil autorizado.
     *
     * Exemplo:
     * ControleAcesso.protegerPagina("ADMINISTRADOR");
     */
    function protegerPagina(perfilPermitido) {

        // Verifica se existe um token armazenado.
        if (!API.estaAutenticado()) {
            window.location.replace("login.html");
            return false;
        }

        // Recupera os dados do usuário autenticado.
        const usuario = API.obterUsuario();

        // Sessão incompleta ou sem perfil identificado.
        if (!usuario || !usuario.perfil) {
            API.encerrarSessao();
            window.location.replace("login.html");
            return false;
        }

        if (usuario.perfil !== perfilPermitido) {
            window.location.replace("login.html?acesso=negado");
            return false;
        }

        // Acesso permitido pela verificação local.
        return true;
    }

    return {
        protegerPagina
    };

})();