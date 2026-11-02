/* ==================================================
   ALTERNA ITENS DO MENU CONFORME A SESSÃO
   ==================================================

   Elementos marcados com:

   data-area="visitante"    → aparecem para quem NÃO está logado
   data-area="autenticado"  → aparecem para quem já está logado

   Este script é incluído em todas as páginas
   para manter o menu consistente.
*/
document.addEventListener("DOMContentLoaded", function () {

    const autenticado = API.estaAutenticado();

    document
        .querySelectorAll('[data-area="visitante"]')
        .forEach(function (elemento) {
            elemento.classList.toggle("d-none", autenticado);
        });

    document
        .querySelectorAll('[data-area="autenticado"]')
        .forEach(function (elemento) {
            elemento.classList.toggle("d-none", !autenticado);
        });
});
