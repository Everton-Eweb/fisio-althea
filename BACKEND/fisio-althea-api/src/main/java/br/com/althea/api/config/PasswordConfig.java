package br.com.althea.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * Classe de configuração responsável por disponibilizar
 * o componente utilizado para gerar o hash das senhas.
 *
 * Tópicos:
 * - Spring Framework
 * - Arquitetura de Software
 * - Engenharia de Software
 */
@Configuration
public class PasswordConfig {

    /*
     * @Bean registra este objeto dentro do Spring.
     *
     * Depois disso, classes como PacienteService
     * poderão receber PasswordEncoder através
     * de injeção de dependência.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {

        /*
         * BCrypt é utilizado para transformar a senha
         * original em um hash antes de salvá-la no banco.
         */
        return new BCryptPasswordEncoder();
    }
}