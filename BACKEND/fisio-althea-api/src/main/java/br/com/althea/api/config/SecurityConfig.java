package br.com.althea.api.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*import org.springframework.security.config.Customizer;*/
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.http.HttpMethod;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/*
 * Configuração central de segurança da API.
 *
 * A partir desta classe:
 *
 * - o endpoint de login será público;
 * - os demais endpoints exigirão autenticação;
 * - o JWT será recebido através do cabeçalho
 *   Authorization: Bearer <token>;
 * - o servidor não utilizará sessão HTTP.
 *
 * Tópicos:
 * - Segurança
 * - Spring Security
 * - APIs REST
 * - Arquitetura de Software
 * - Engenharia de Software
 */
@Configuration
public class SecurityConfig {

        /*
         * Configura quais endpoints são públicos
         * e quais exigem autenticação.
         */
        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http) throws Exception {

                http

                                /*
                                 * Desabilitamos CSRF porque nossa API REST
                                 * utilizará autenticação stateless por JWT,
                                 * e não sessão baseada em formulário.
                                 */
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * Define que o servidor não deve criar
                                 * sessão para guardar autenticação.
                                 *
                                 * Cada requisição deverá apresentar
                                 * seu próprio token JWT.
                                 */
                                .sessionManagement(session -> session.sessionCreationPolicy(
                                                SessionCreationPolicy.STATELESS))

                                /*
                                 * Define as regras de autorização
                                 * de acordo com o endpoint e o perfil.
                                 */
                                .authorizeHttpRequests(auth -> auth

                                                /*
                                                 * =====================================================
                                                 * OPENAPI / SWAGGER
                                                 * =====================================================
                                                 *
                                                 * Libera somente os recursos responsáveis
                                                 * pela documentação da API.
                                                 *
                                                 * Isso permite abrir o Swagger sem possuir
                                                 * um token JWT.
                                                 *
                                                 * Os endpoints reais da aplicação continuam
                                                 * protegidos pelas regras abaixo.
                                                 *
                                                 * Tópicos:
                                                 * - APIs REST
                                                 * - Spring Security
                                                 * - Spring Boot
                                                 * - Engenharia de Software
                                                 * - Testes de Software
                                                 */
                                                .requestMatchers(
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**",
                                                                "/swagger-ui.html")
                                                .permitAll()

                                                /*
                                                 * O login precisa ser público.
                                                 */
                                                .requestMatchers("/api/auth/login")
                                                .permitAll()

                                                /*
                                                 * Permite o cadastro inicial de paciente
                                                 * sem exigir autenticação.
                                                 *
                                                 * Isso possibilita que um novo paciente
                                                 * crie sua conta.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/pacientes")
                                                .permitAll()

                                                /*
                                                 * O paciente autenticado pode consultar
                                                 * somente os próprios dados através de /me.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/pacientes/me")
                                                .hasRole("PACIENTE")

                                                /*
                                                 * A consulta da lista completa de pacientes
                                                 * fica restrita aos perfis administrativos.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/pacientes")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * Consulta de um paciente específico por ID.
                                                 *
                                                 * Também ficará restrita aos perfis
                                                 * administrativo e recepção.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/pacientes/*")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * =====================================================
                                                 * PROFISSIONAIS
                                                 * =====================================================
                                                 */

                                                /*
                                                 * O cadastro de novos profissionais fica
                                                 * restrito ao perfil ADMINISTRADOR.
                                                 *
                                                 * Um paciente, profissional ou recepcionista
                                                 * não pode criar uma nova conta profissional.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Controle de acesso
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/profissionais")
                                                .hasRole("ADMINISTRADOR")

                                                /*
                                                 * Somente um usuário com perfil PROFISSIONAL
                                                 * pode acessar o endpoint /me de profissional.
                                                 *
                                                 * O próprio JWT determinará qual profissional
                                                 * será retornado.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/profissionais/me")
                                                .hasRole("PROFISSIONAL")

                                                /*
                                                 * A lista de profissionais pode ser consultada
                                                 * por qualquer usuário autenticado.
                                                 *
                                                 * Isso será necessário, por exemplo, para que
                                                 * pacientes possam visualizar os profissionais
                                                 * disponíveis para atendimento.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/profissionais")
                                                .authenticated()

                                                /*
                                                 * A consulta individual de profissional por ID
                                                 * também fica disponível para usuários autenticados.
                                                 *
                                                 * A regra /me precisa aparecer antes desta,
                                                 * pois /me também possui um segmento após
                                                 * /api/profissionais/.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/profissionais/*")
                                                .authenticated()

                                                /*
                                                 * =====================================================
                                                 * SERVIÇOS
                                                 * =====================================================
                                                 */

                                                /*
                                                 * Todos os usuários autenticados podem
                                                 * consultar a relação de serviços.
                                                 *
                                                 * Exemplos:
                                                 * PACIENTE
                                                 * PROFISSIONAL
                                                 * RECEPCIONISTA
                                                 * ADMINISTRADOR
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/servicos/**")
                                                .authenticated()

                                                /*
                                                 * Cadastro de serviços.
                                                 *
                                                 * Somente ADMINISTRADOR e RECEPCIONISTA
                                                 * podem cadastrar novos serviços.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/servicos")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * Alteração de serviços.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/servicos/**")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * Desativação de serviços.
                                                 *
                                                 * No nosso sistema o DELETE realiza
                                                 * exclusão lógica, alterando ativo para false.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/servicos/**")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * =====================================================
                                                 * DISPONIBILIDADES
                                                 * =====================================================
                                                 */

                                                /*
                                                 * Todos os usuários autenticados podem consultar
                                                 * as disponibilidades dos profissionais.
                                                 *
                                                 * Isso é necessário para que o paciente consiga
                                                 * visualizar os horários disponíveis antes
                                                 * de realizar um agendamento.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/disponibilidades/**")
                                                .authenticated()

                                                /*
                                                 * O cadastro de disponibilidade pode ser realizado por:
                                                 *
                                                 * - ADMINISTRADOR;
                                                 * - RECEPCIONISTA;
                                                 * - PROFISSIONAL.
                                                 *
                                                 * O profissional poderá cadastrar somente
                                                 * a própria disponibilidade.
                                                 *
                                                 * Essa segunda validação será implementada
                                                 * posteriormente na camada de negócio.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/disponibilidades")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PROFISSIONAL")

                                                /*
                                                 * =====================================================
                                                 * AGENDAMENTOS
                                                 * =====================================================
                                                 */

                                                /*
                                                 * PACIENTE e PROFISSIONAL podem acessar
                                                 * somente os agendamentos relacionados
                                                 * ao próprio usuário através de /me.
                                                 *
                                                 * O idUsuario utilizado na consulta
                                                 * será obtido diretamente do JWT.
                                                 *
                                                 * IMPORTANTE:
                                                 * esta regra deve aparecer antes de
                                                 * /api/agendamentos/*.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/agendamentos/me")
                                                .hasAnyRole(
                                                                "PACIENTE",
                                                                "PROFISSIONAL")

                                                /*
                                                 * A consulta da lista completa de agendamentos
                                                 * fica disponível somente para os perfis
                                                 * administrativos.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/agendamentos")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * A consulta de um agendamento específico
                                                 * pelo ID também fica, por enquanto,
                                                 * restrita aos perfis administrativos.
                                                 *
                                                 * PACIENTE e PROFISSIONAL utilizarão /me
                                                 * para não conseguirem consultar registros
                                                 * de outras pessoas apenas alterando o ID.
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/agendamentos/*")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")

                                                /*
                                                 * Criação de agendamentos.
                                                 *
                                                 * Podem criar:
                                                 *
                                                 * - PACIENTE:
                                                 * somente para si próprio, conforme validação
                                                 * realizada no AgendamentoService.
                                                 *
                                                 * - RECEPCIONISTA:
                                                 * pode criar para pacientes.
                                                 *
                                                 * - ADMINISTRADOR:
                                                 * pode criar para pacientes.
                                                 *
                                                 * O PROFISSIONAL não cria novos agendamentos.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/agendamentos")
                                                .hasAnyRole(
                                                                "PACIENTE",
                                                                "RECEPCIONISTA",
                                                                "ADMINISTRADOR")

                                                /*
                                                 * Confirmação de agendamento.
                                                 *
                                                 * Podem confirmar:
                                                 *
                                                 * - ADMINISTRADOR;
                                                 * - RECEPCIONISTA;
                                                 * - PROFISSIONAL.
                                                 *
                                                 * O PROFISSIONAL ainda passa por uma segunda
                                                 * validação na AgendamentoService para garantir
                                                 * que o agendamento pertence a ele.
                                                 *
                                                 * PACIENTE não possui permissão para confirmar.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/agendamentos/*/confirmar")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PROFISSIONAL")

                                                /*
                                                 * Cancelamento de agendamento.
                                                 *
                                                 * Podem solicitar o cancelamento:
                                                 *
                                                 * - ADMINISTRADOR;
                                                 * - RECEPCIONISTA;
                                                 * - PACIENTE;
                                                 * - PROFISSIONAL.
                                                 *
                                                 * PACIENTE e PROFISSIONAL ainda passam
                                                 * por uma segunda validação na Service,
                                                 * que verifica se o agendamento pertence
                                                 * ao usuário autenticado.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/agendamentos/*/cancelar")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PACIENTE",
                                                                "PROFISSIONAL")

                                                /*
                                                 * Reagendamento.
                                                 *
                                                 * Podem solicitar:
                                                 *
                                                 * - ADMINISTRADOR;
                                                 * - RECEPCIONISTA;
                                                 * - PACIENTE;
                                                 * - PROFISSIONAL.
                                                 *
                                                 * PACIENTE e PROFISSIONAL ainda passam
                                                 * pela validação de propriedade realizada
                                                 * na AgendamentoService.
                                                 *
                                                 * Depois dessa autorização, continuam
                                                 * sendo verificadas as regras da RN04:
                                                 *
                                                 * - disponibilidade;
                                                 * - conflito no novo horário.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/agendamentos/*/reagendar")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PACIENTE",
                                                                "PROFISSIONAL")

                                                /*
                                                 * =====================================================
                                                 * MENSAGENS
                                                 * =====================================================
                                                 */

                                                /*
                                                 * O PACIENTE pode consultar somente
                                                 * as mensagens relacionadas ao próprio cadastro
                                                 * através do endpoint /me.
                                                 *
                                                 * O idUsuario utilizado na consulta
                                                 * vem diretamente do JWT.
                                                 *
                                                 * IMPORTANTE:
                                                 * esta regra deve aparecer antes de
                                                 * /api/mensagens/*.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/mensagens/me")
                                                .hasRole("PACIENTE")

                                                /*
                                                 * GET /api/mensagens
                                                 *
                                                 * A listagem completa de mensagens fica restrita
                                                 * aos perfis administrativos.
                                                 *
                                                 * PACIENTE utiliza /api/mensagens/me para consultar
                                                 * somente as mensagens relacionadas ao próprio cadastro.
                                                 *
                                                 * PROFISSIONAL não possui acesso à listagem geral.
                                                 *
                                                 * Tópicos:
                                                 * - APIs REST
                                                 * - Spring Boot
                                                 * - Arquitetura de Software
                                                 * - Levantamento e Especificação de Requisitos
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/mensagens")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA")


                                                /*
                                                 * Consulta uma mensagem específica.
                                                 *
                                                 * ADMINISTRADOR e RECEPCIONISTA:
                                                 * podem consultar qualquer mensagem.
                                                 *
                                                 * PACIENTE:
                                                 * pode acessar o endpoint, mas a
                                                 * MensagemService verifica se a mensagem
                                                 * realmente pertence ao seu cadastro.
                                                 *
                                                 * PROFISSIONAL:
                                                 * permanece sem acesso nesta operação.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/mensagens/*")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PACIENTE")

                                                /*
                                                 * Marca uma mensagem como lida.
                                                 *
                                                 * Podem executar:
                                                 *
                                                 * - ADMINISTRADOR;
                                                 * - RECEPCIONISTA;
                                                 * - PACIENTE.
                                                 *
                                                 * O PACIENTE ainda passa por uma segunda
                                                 * validação na MensagemService para garantir
                                                 * que a mensagem pertence ao próprio cadastro.
                                                 *
                                                 * O PROFISSIONAL não possui acesso a essa
                                                 * operação no modelo atual.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/mensagens/*/lida")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PACIENTE")

                                                /*
                                                 * =====================================================
                                                 * ENVIO DE MENSAGENS
                                                 * =====================================================
                                                 *
                                                 * Todos os perfis existentes no sistema podem
                                                 * enviar mensagens.
                                                 *
                                                 * Entretanto, a autorização não termina aqui.
                                                 *
                                                 * Caso o usuário seja PACIENTE, a MensagemService
                                                 * também verifica se o idPaciente informado
                                                 * corresponde ao próprio cadastro.
                                                 *
                                                 * Tópicos:
                                                 * - Segurança
                                                 * - Spring Security
                                                 * - APIs REST
                                                 * - Engenharia de Software
                                                 */
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/mensagens")
                                                .hasAnyRole(
                                                                "ADMINISTRADOR",
                                                                "RECEPCIONISTA",
                                                                "PROFISSIONAL",
                                                                "PACIENTE")

                                                /*
                                                 * Os demais endpoints continuam exigindo
                                                 * apenas autenticação por enquanto.
                                                 */
                                                .anyRequest()
                                                .authenticated())

                                /*
                                 * Configura a autenticação através de JWT
                                 * e utiliza nosso conversor de perfis.
                                 */
                                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(
                                                jwtAuthenticationConverter())));

                return http.build();
        }

        /*
         * Configura como os tokens JWT serão validados.
         *
         * A chave utilizada aqui precisa ser exatamente
         * a mesma utilizada pelo JwtService para assinar
         * os tokens.
         */
        @Bean
        public JwtDecoder jwtDecoder(
                        @Value("${app.jwt.secret}") String secret) {

                /*
                 * Converte a String configurada no
                 * application.properties para SecretKey.
                 */
                SecretKey chave = new SecretKeySpec(
                                secret.getBytes(StandardCharsets.UTF_8),
                                "HmacSHA256");

                /*
                 * Cria o decodificador responsável
                 * por verificar a assinatura HS256.
                 */
                NimbusJwtDecoder decoder = NimbusJwtDecoder
                                .withSecretKey(chave)
                                .macAlgorithm(MacAlgorithm.HS256)
                                .build();

                /*
                 * Além da assinatura e da expiração,
                 * validamos também quem emitiu o token.
                 *
                 * Esse valor precisa corresponder ao issuer
                 * definido no JwtService:
                 *
                 * issuer("fisio-althea-api")
                 */
                decoder.setJwtValidator(
                                JwtValidators.createDefaultWithIssuer(
                                                "fisio-althea-api"));

                return decoder;
        }

        /*
         * Converte o claim "perfil" existente no JWT
         * em uma autoridade reconhecida pelo Spring Security.
         *
         * Exemplo:
         *
         * JWT:
         * perfil = PACIENTE
         *
         * Spring Security:
         * ROLE_PACIENTE
         *
         * Isso permitirá posteriormente utilizar regras como:
         *
         * hasRole("PACIENTE")
         * hasRole("PROFISSIONAL")
         * hasRole("RECEPCIONISTA")
         * hasRole("ADMINISTRADOR")
         *
         * Tópicos:
         * - Segurança
         * - Spring Security
         * - Arquitetura de Software
         * - Controle de acesso
         */
        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {

                /*
                 * Define de qual claim do JWT serão
                 * recuperadas as permissões do usuário.
                 *
                 * Nosso JwtService grava:
                 *
                 * "perfil": "PACIENTE"
                 */
                JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

                authoritiesConverter.setAuthoritiesClaimName("perfil");

                /*
                 * O prefixo ROLE_ é o padrão utilizado
                 * pelo Spring Security quando trabalhamos
                 * com hasRole(...).
                 *
                 * PACIENTE
                 * ↓
                 * ROLE_PACIENTE
                 */
                authoritiesConverter.setAuthorityPrefix("ROLE_");

                /*
                 * Configura o conversor principal
                 * da autenticação JWT.
                 */
                JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();

                authenticationConverter.setJwtGrantedAuthoritiesConverter(
                                authoritiesConverter);

                return authenticationConverter;
        }
}