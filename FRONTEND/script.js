/* ==================================================
   FORMULÁRIO DE AGENDAMENTO + N8N
   ================================================== */

/*
    Seleciona o formulário pelo ID
    definido no HTML.
*/
const formularioAgendamento =
    document.querySelector("#formAgendamento");


const mensagemFormulario =
    document.querySelector("#mensagem-formulario");


/*
    Só executa o código caso o formulário
    realmente exista na página.
*/
if (formularioAgendamento) {

    /*
        Escuta o envio do formulário.
        Como utilizaremos "await", a função
        precisa ser declarada como async.
    */
    formularioAgendamento.addEventListener(
        "submit",
        async function (evento) {

            /*
                Impede o comportamento padrão do formulário,
                que seria recarregar a página.
            */
            evento.preventDefault();

            // Limpa qualquer mensagem de uma tentativa anterior.
            mensagemFormulario.textContent = "";


            /* ==========================================
               CAPTURA DOS DADOS
               ========================================== */

            const nome =
                document.querySelector("#nome").value.trim();

            const telefone =
                document.querySelector("#telefone").value.trim();

            // Remove espaços, parênteses, hífens etc.
            let telefoneNumerico = telefone.replace(/\D/g, "");

            // Exemplo: 05554991953370
            if (
                telefoneNumerico.length === 14 &&
                telefoneNumerico.startsWith("055")
            ) {
                telefoneNumerico = telefoneNumerico.substring(3);
            }

            // Exemplo: +55 54 99195-3370
            if (
                telefoneNumerico.length === 13 &&
                telefoneNumerico.startsWith("55")
            ) {
                telefoneNumerico = telefoneNumerico.substring(2);
            }

            // Exemplo: 054 99195-3370
            if (
                telefoneNumerico.length === 12 &&
                telefoneNumerico.startsWith("0")
            ) {
                telefoneNumerico = telefoneNumerico.substring(1);
            }

            // Depois da normalização deve restar DDD + celular.
            // Lista de DDDs brasileiros válidos.
            const dddsValidos = [
                "11", "12", "13", "14", "15", "16", "17", "18", "19",
                "21", "22", "24",
                "27", "28",
                "31", "32", "33", "34", "35", "37", "38",
                "41", "42", "43", "44", "45", "46",
                "47", "48", "49",
                "51", "53", "54", "55",
                "61", "62", "63", "64", "65", "66", "67", "68", "69",
                "71", "73", "74", "75", "77", "79",
                "81", "82", "83", "84", "85", "86", "87", "88", "89",
                "91", "92", "93", "94", "95", "96", "97", "98", "99"
            ];

            // Separa o DDD dos dois primeiros números.
            const ddd = telefoneNumerico.substring(0, 2);

            // Pega o número depois do DDD.
            const numeroCelular = telefoneNumerico.substring(2);

            // Verifica:
            // 1. Se possui 11 dígitos.
            // 2. Se o DDD existe.
            // 3. Se o celular possui 9 dígitos.
            // 4. Se o celular começa com 9.
            if (
                telefoneNumerico.length !== 11 ||
                !dddsValidos.includes(ddd) ||
                numeroCelular.length !== 9 ||
                !numeroCelular.startsWith("9")
            ) {
                alert("Informe um número de celular válido com DDD.");

                document.querySelector("#telefone").focus();

                return;
            }

            const servico =
                document.querySelector("#servico").value;

            const mensagem =
                document.querySelector("#mensagem").value.trim();


            /* ==========================================
               ORGANIZAÇÃO DOS DADOS
               ========================================== */

            /*
                Criamos um objeto JavaScript contendo
                exatamente as informações que queremos
                enviar ao n8n.
            */
            const dadosAgendamento = {
                nome: nome,
                telefone: telefoneNumerico,
                servico: servico,
                mensagem: mensagem
            };


            /* ==========================================
            ENVIO PARA O N8N
            ========================================== */

            try {

                /*
                    fetch faz uma requisição HTTP
                    para o Webhook do n8n.
                */
                const resposta = await fetch(
                    "https://n8n.cassul.com.br:5680/webhook-test/fisio-althea-agendamento",
                    {
                        /*
                            POST indica que estamos
                            enviando informações.
                        */
                        method: "POST",

                        /*
                            Informa ao n8n que os dados
                            estão no formato JSON.
                        */
                        headers: {
                            "Content-Type": "application/json"
                        },

                        /*
                            Converte o objeto JavaScript
                            para JSON antes do envio.
                        */
                        body: JSON.stringify(dadosAgendamento)
                    }
                );


                /* ==========================================
                VERIFICAÇÃO DA RESPOSTA HTTP
                ========================================== */

                /*
                    Caso o n8n retorne um erro HTTP,
                    interrompemos o processo.
                */
                if (!resposta.ok) {

                    throw new Error(
                        "Não foi possível enviar o agendamento."
                    );
                }


                /* ==========================================
                RESPOSTA DO N8N
                ========================================== */

                /*
                    Converte o JSON retornado pelo n8n
                    para um objeto JavaScript.
                */
                const resultado = await resposta.json();


                /*
                    Se o número não possuir WhatsApp,
                    mostramos a mensagem retornada pelo n8n
                    e interrompemos o envio.
                */
                if (!resultado.sucesso) {

                    // Exibe a mensagem retornada pelo n8n dentro do formulário.
                    mensagemFormulario.textContent =
                        resultado.mensagem;

                    document
                        .querySelector("#telefone")
                        .focus();

                    return;
                }


                /* ==========================================
                SUCESSO
                ========================================== */

                /*
                    Se chegou até aqui, o número existe
                    no WhatsApp e a mensagem foi enviada.
                */
                mensagemFormulario.textContent =
                    resultado.mensagem;

                console.log(
                    "Agendamento enviado ao n8n:",
                    dadosAgendamento
                );

                /*
                    Limpa os campos do formulário
                    somente depois do envio com sucesso.
                */
                formularioAgendamento.reset();


            } catch (erro) {

                /* ==========================================
                ERRO
                ========================================== */

                console.error(
                    "Erro ao enviar o formulário:",
                    erro
                );

            }

        }
    );
}

/*
    ==================================================
    CARROSSEL 3D DE TRATAMENTOS
    ==================================================
*/

/*
    Seleciona os principais elementos do carrossel.
*/
const trilhaTratamentos =
    document.querySelector("#trilhaTratamentos");

const botaoAnterior =
    document.querySelector("#tratamentoAnterior");

const botaoProximo =
    document.querySelector("#proximoTratamento");

const botaoPausaCarrossel =
    document.querySelector("#alternarCarrossel");

const areaCarrossel =
    document.querySelector(".carrossel-tratamentos");

const statusCarrossel =
    document.querySelector("#statusCarrossel");


/*
    O carrossel será inicializado somente se todos
    os elementos indispensáveis existirem no HTML.
*/
if (
    trilhaTratamentos &&
    botaoAnterior &&
    botaoProximo &&
    botaoPausaCarrossel &&
    areaCarrossel
) {

    /*
        Tempo entre as trocas automáticas.

        5000 milissegundos equivalem a 5 segundos.
        Para usar 3 segundos, altere para 3000.
    */
    const tempoRotacao = 5000;

    /*
        Guardará o identificador do setInterval.
    */
    let temporizadorCarrossel = null;

    /*
        Indica se o usuário pausou manualmente
        utilizando o botão Pausar.
    */
    let carrosselPausadoPeloUsuario = false;

    /*
        Controlam pausas temporárias provocadas
        pelo mouse ou pela navegação com teclado.
    */
    let mouseSobreCarrossel = false;
    let focoDentroCarrossel = false;


    /*
        Atualiza o card central e controla quais
        elementos podem receber foco pela tecla Tab.
    */
    function atualizarCardAtivo() {

        const itensCarrossel =
            trilhaTratamentos.querySelectorAll(
                ".item-carrossel"
            );

        itensCarrossel.forEach(function (item, indice) {

            /*
                O primeiro elemento da trilha é exibido
                no centro e será considerado ativo.
            */
            const estaAtivo = indice === 0;

            /*
                Adiciona ou remove a classe visual.
            */
            item.classList.toggle(
                "item-ativo",
                estaAtivo
            );

            /*
                Informa às tecnologias assistivas
                qual item está atualmente selecionado.
            */
            if (estaAtivo) {

                item.setAttribute(
                    "aria-current",
                    "true"
                );

            } else {

                item.removeAttribute("aria-current");
            }

            /*
                Procura links, botões e campos dentro do card.
            */
            const elementosInterativos =
                item.querySelectorAll(
                    "a, button, input, select, textarea"
                );

            elementosInterativos.forEach(
                function (elemento) {

                    if (estaAtivo) {

                        /*
                            O elemento do card central volta
                            à ordem normal de navegação.
                        */
                        elemento.removeAttribute("tabindex");

                    } else {

                        /*
                            Os controles dos cards laterais
                            não recebem foco pela tecla Tab.
                        */
                        elemento.setAttribute(
                            "tabindex",
                            "-1"
                        );
                    }
                }
            );
        });
    }


    /*
        Anuncia o tratamento escolhido manualmente
        para pessoas que usam leitores de tela.
    */
    function anunciarTratamentoAtivo() {

        /*
            Se a área de status não existir,
            a função é encerrada sem gerar erro.
        */
        if (!statusCarrossel) {
            return;
        }

        const cardAtivo =
            trilhaTratamentos.querySelector(
                ".item-ativo"
            );

        const tituloTratamento =
            cardAtivo?.querySelector(".card-title");

        /*
            Evita erro caso o card ou o título não existam.
        */
        if (!tituloTratamento) {
            return;
        }

        statusCarrossel.textContent =
            `Tratamento selecionado: ${tituloTratamento.textContent.trim()}`;
    }


    /*
        Move o primeiro card para o final da trilha.

        Exemplo:
        1, 2, 3, 4
        passa a ser:
        2, 3, 4, 1
    */
    function mostrarProximoTratamento() {

        const primeiroCard =
            trilhaTratamentos.firstElementChild;

        if (!primeiroCard) {
            return;
        }

        trilhaTratamentos.appendChild(
            primeiroCard
        );

        atualizarCardAtivo();
    }


    /*
        Move o último card para o início da trilha.

        Exemplo:
        1, 2, 3, 4
        passa a ser:
        4, 1, 2, 3
    */
    function mostrarTratamentoAnterior() {

        const ultimoCard =
            trilhaTratamentos.lastElementChild;

        const primeiroCard =
            trilhaTratamentos.firstElementChild;

        if (!ultimoCard || !primeiroCard) {
            return;
        }

        trilhaTratamentos.insertBefore(
            ultimoCard,
            primeiroCard
        );

        atualizarCardAtivo();
    }


    /*
        Inicia ou reinicia a rotação automática.
    */
    function iniciarRotacaoAutomatica() {

        /*
            Cancela um temporizador anterior para evitar
            dois intervalos funcionando simultaneamente.
        */
        clearInterval(temporizadorCarrossel);

        temporizadorCarrossel = setInterval(
            mostrarProximoTratamento,
            tempoRotacao
        );
    }


    /*
        Interrompe a rotação automática.
    */
    function pausarRotacaoAutomatica() {

        clearInterval(temporizadorCarrossel);

        temporizadorCarrossel = null;
    }


    /*
        Retoma a rotação somente quando:

        - o usuário não pausou manualmente;
        - o mouse não está sobre o carrossel;
        - o foco do teclado não está dentro dele.
    */
    function tentarRetomarRotacao() {

        const podeRotacionar =
            !carrosselPausadoPeloUsuario &&
            !mouseSobreCarrossel &&
            !focoDentroCarrossel;

        if (podeRotacionar) {
            iniciarRotacaoAutomatica();
        }
    }


    /*
        Atualiza o texto e os atributos de acessibilidade
        do botão Pausar/Retomar.
    */
    function atualizarBotaoPausa() {

        if (carrosselPausadoPeloUsuario) {

            botaoPausaCarrossel.textContent =
                "Retomar";

            botaoPausaCarrossel.setAttribute(
                "aria-label",
                "Retomar rotação automática"
            );

            botaoPausaCarrossel.setAttribute(
                "aria-pressed",
                "true"
            );

        } else {

            botaoPausaCarrossel.textContent =
                "Pausar";

            botaoPausaCarrossel.setAttribute(
                "aria-label",
                "Pausar rotação automática"
            );

            botaoPausaCarrossel.setAttribute(
                "aria-pressed",
                "false"
            );
        }
    }


    /*
        Reinicia a contagem depois de uma ação manual,
        respeitando as pausas atuais.
    */
    function reiniciarContagemAutomatica() {

        pausarRotacaoAutomatica();
        tentarRetomarRotacao();
    }


    /*
        ==================================================
        CLIQUE NOS CARDS LATERAIS
        ==================================================
    */

    const itensTratamentos =
        trilhaTratamentos.querySelectorAll(
            ".item-carrossel"
        );

    /*
        Adiciona um evento de clique a cada card.
    */
    itensTratamentos.forEach(
        function (itemClicado) {

            itemClicado.addEventListener(
                "click",
                function () {

                    /*
                        Se o card já estiver no centro,
                        não é necessário reposicioná-lo.

                        O link Saiba mais continuará
                        funcionando normalmente.
                    */
                    if (
                        itemClicado.classList.contains(
                            "item-ativo"
                        )
                    ) {
                        return;
                    }

                    /*
                        Cria uma lista com a ordem atual
                        dos cards dentro da trilha.
                    */
                    const itensAtuais =
                        Array.from(
                            trilhaTratamentos.children
                        );

                    const indiceClicado =
                        itensAtuais.indexOf(
                            itemClicado
                        );

                    if (indiceClicado < 0) {
                        return;
                    }

                    /*
                        Calcula quantos movimentos seriam
                        necessários em cada direção.
                    */
                    const distanciaAvancando =
                        indiceClicado;

                    const distanciaVoltando =
                        itensAtuais.length -
                        indiceClicado;

                    /*
                        Usa o caminho que exige menos movimentos.
                    */
                    if (
                        distanciaVoltando <
                        distanciaAvancando
                    ) {

                        for (
                            let movimento = 0;
                            movimento <
                            distanciaVoltando;
                            movimento++
                        ) {

                            const ultimoCard =
                                trilhaTratamentos
                                    .lastElementChild;

                            trilhaTratamentos.insertBefore(
                                ultimoCard,
                                trilhaTratamentos
                                    .firstElementChild
                            );
                        }

                    } else {

                        for (
                            let movimento = 0;
                            movimento <
                            distanciaAvancando;
                            movimento++
                        ) {

                            trilhaTratamentos.appendChild(
                                trilhaTratamentos
                                    .firstElementChild
                            );
                        }
                    }

                    /*
                        Atualiza o card central,
                        anuncia a mudança e reinicia
                        a contagem automática.
                    */
                    atualizarCardAtivo();
                    anunciarTratamentoAtivo();
                    reiniciarContagemAutomatica();
                }
            );
        }
    );


    /*
        ==================================================
        CONTROLES MANUAIS
        ==================================================
    */

    /*
        Botão para exibir o próximo tratamento.
    */
    botaoProximo.addEventListener(
        "click",
        function () {

            mostrarProximoTratamento();
            anunciarTratamentoAtivo();
            reiniciarContagemAutomatica();
        }
    );


    /*
        Botão para exibir o tratamento anterior.
    */
    botaoAnterior.addEventListener(
        "click",
        function () {

            mostrarTratamentoAnterior();
            anunciarTratamentoAtivo();
            reiniciarContagemAutomatica();
        }
    );


    /*
        Botão para pausar ou retomar a rotação.
    */
    botaoPausaCarrossel.addEventListener(
        "click",
        function () {

            carrosselPausadoPeloUsuario =
                !carrosselPausadoPeloUsuario;

            if (carrosselPausadoPeloUsuario) {

                pausarRotacaoAutomatica();

            } else {

                /*
                    Não inicia diretamente.

                    A função verifica se o mouse ou
                    o foco ainda estão dentro do carrossel.
                */
                tentarRetomarRotacao();
            }

            atualizarBotaoPausa();
        }
    );


    /*
        ==================================================
        PAUSA DURANTE A INTERAÇÃO
        ==================================================
    */

    /*
        Pausa quando o mouse entra no carrossel.
    */
    areaCarrossel.addEventListener(
        "mouseenter",
        function () {

            mouseSobreCarrossel = true;

            pausarRotacaoAutomatica();
        }
    );


    /*
        Tenta retomar quando o mouse sai.
    */
    areaCarrossel.addEventListener(
        "mouseleave",
        function () {

            mouseSobreCarrossel = false;

            tentarRetomarRotacao();
        }
    );


    /*
        Pausa quando algum elemento interno
        recebe foco pelo teclado.
    */
    areaCarrossel.addEventListener(
        "focusin",
        function () {

            focoDentroCarrossel = true;

            pausarRotacaoAutomatica();
        }
    );


    /*
        Verifica se o foco saiu completamente
        da área do carrossel.
    */
    areaCarrossel.addEventListener(
        "focusout",
        function (evento) {

            const novoElementoFocado =
                evento.relatedTarget;

            focoDentroCarrossel =
                novoElementoFocado !== null &&
                areaCarrossel.contains(
                    novoElementoFocado
                );

            tentarRetomarRotacao();
        }
    );


    /*
        ==================================================
        NAVEGAÇÃO PELO TECLADO
        ==================================================
    */

    areaCarrossel.addEventListener(
        "keydown",
        function (evento) {

            /*
                As setas controlam o carrossel somente
                quando o foco está na própria região.

                Isso evita interferir em links
                ou botões internos.
            */
            if (evento.target !== areaCarrossel) {
                return;
            }

            /*
                Seta para a direita:
                exibe o próximo tratamento.
            */
            if (evento.key === "ArrowRight") {

                evento.preventDefault();

                mostrarProximoTratamento();
                anunciarTratamentoAtivo();
                reiniciarContagemAutomatica();
            }

            /*
                Seta para a esquerda:
                exibe o tratamento anterior.
            */
            if (evento.key === "ArrowLeft") {

                evento.preventDefault();

                mostrarTratamentoAnterior();
                anunciarTratamentoAtivo();
                reiniciarContagemAutomatica();
            }
        }
    );


    /*
        Pausa quando a aba do navegador fica oculta.

        Quando o visitante retorna, tenta retomar
        respeitando as outras condições de pausa.
    */
    document.addEventListener(
        "visibilitychange",
        function () {

            if (document.hidden) {

                pausarRotacaoAutomatica();

            } else {

                tentarRetomarRotacao();
            }
        }
    );


    /*
        ==================================================
        ESTADO INICIAL
        ==================================================
    */

    atualizarCardAtivo();
    atualizarBotaoPausa();
    iniciarRotacaoAutomatica();
}

/* ==================================================
   EFEITO PARALLAX DO BANNER
   ================================================== */

/*
    Seleciona o banner principal.
*/
const bannerPrincipal =
    document.querySelector(".banner-principal");

/*
    Verifica se o usuário solicitou menos animações
    nas configurações de acessibilidade do sistema.
*/
const prefereMenosMovimento =
    window.matchMedia(
        "(prefers-reduced-motion: reduce)"
    ).matches;

/*
    Só cria o efeito quando o banner existe
    e o usuário não pediu redução de movimentos.
*/
if (bannerPrincipal && !prefereMenosMovimento) {

    let atualizacaoPendente = false;

    function atualizarParallax() {

        /*
            Obtém a posição atual do banner
            em relação à janela do navegador.
        */
        const posicaoBanner =
            bannerPrincipal.getBoundingClientRect();

        /*
            Só calcula o efeito enquanto o banner
            estiver próximo da área visível.
        */
        const bannerVisivel =
            posicaoBanner.bottom > 0 &&
            posicaoBanner.top < window.innerHeight;

        if (bannerVisivel) {

            /*
                Multiplicamos por um valor pequeno para
                manter o movimento discreto.

                Quanto maior 0.10, mais forte será o efeito.
            */
            const deslocamento =
                posicaoBanner.top * -0.20;

            bannerPrincipal.style.setProperty(
                "--parallax-y",
                `${deslocamento}px`
            );
        }

        atualizacaoPendente = false;
    }

    /*
        requestAnimationFrame evita cálculos excessivos
        durante a rolagem.
    */
    window.addEventListener(
        "scroll",
        function () {

            if (!atualizacaoPendente) {

                window.requestAnimationFrame(
                    atualizarParallax
                );

                atualizacaoPendente = true;
            }
        },
        { passive: true }
    );

    /*
        Define a posição inicial da imagem.
    */
    atualizarParallax();
}