package br.com.althea.api.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/*
 * Configuração da documentação OpenAPI / Swagger.
 *
 * Aqui definimos:
 *
 * - nome e descrição da API;
 * - versão da documentação;
 * - autenticação através de Bearer Token JWT.
 *
 * Tópicos:
 * - APIs REST
 * - Spring Boot
 * - Spring Security
 * - Segurança
 * - Engenharia de Software
 * - Testes de Software
 */
@Configuration

/*
 * Informações gerais exibidas no topo do Swagger.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Fisio Althea API",
                version = "1.0",
                description = "API REST para gerenciamento de pacientes, "
                        + "profissionais, serviços, disponibilidades, "
                        + "agendamentos e mensagens da Clínica Fisio Althea."
        ),

        /*
         * Define que, por padrão, os endpoints
         * utilizam o esquema de autenticação bearerAuth.
         */
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)

/*
 * Define para o Swagger como funciona
 * nossa autenticação.
 *
 * HTTP
 *   ↓
 * Bearer
 *   ↓
 * JWT
 */
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Informe o token JWT obtido em /api/auth/login"
)
public class OpenApiConfig {

    /*
     * Não precisamos criar métodos nesta classe.
     *
     * As próprias anotações configuram
     * a documentação OpenAPI.
     */
}