package br.com.althea.api.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import br.com.althea.api.entity.Usuario;

/*
 * Serviço responsável pela geração
 * dos tokens JWT da aplicação.
 *
 * O token será criado somente depois
 * que o usuário informar credenciais válidas.
 *
 * Tópicos:
 * - Segurança
 * - Spring Security
 * - APIs REST
 * - Arquitetura de Software
 * - Programação Orientada a Objetos
 */
@Service
public class JwtService {

    /*
     * Chave utilizada para assinar o JWT.
     *
     * O valor vem do application.properties:
     *
     * app.jwt.secret=...
     */
    private final String secret;


    /*
     * Tempo de validade do token, em minutos.
     *
     * Também vem do application.properties:
     *
     * app.jwt.expiration-minutes=120
     */
    private final long expirationMinutes;


    /*
     * Construtor utilizado pelo Spring
     * para injetar as configurações.
     */
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {

        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }


    /*
     * Gera um token JWT para o usuário autenticado.
     */
    public String gerarToken(Usuario usuario) {

        /*
         * Horário atual.
         */
        Instant agora = Instant.now();


        /*
         * Calcula quando o token irá expirar.
         *
         * Exemplo:
         *
         * agora = 10:00
         * validade = 120 minutos
         *
         * expiração = 12:00
         */
        Instant expiracao =
                agora.plus(expirationMinutes, ChronoUnit.MINUTES);


        /*
         * Transforma a chave configurada
         * em uma SecretKey compatível com HS256.
         */
        SecretKey chave = new SecretKeySpec(
                secret.getBytes(),
                "HmacSHA256"
        );


        /*
         * Cria o componente responsável
         * por assinar o token.
         */
        JwtEncoder jwtEncoder =
                NimbusJwtEncoder
                        .withSecretKey(chave)
                        .build();


        /*
         * Define as informações presentes
         * dentro do JWT.
         *
         * IMPORTANTE:
         * nunca colocamos a senha no token.
         */
        JwtClaimsSet claims = JwtClaimsSet.builder()

                /*
                 * Identifica quem emitiu o token.
                 */
                .issuer("fisio-althea-api")

                /*
                 * Identifica o usuário autenticado.
                 *
                 * Utilizamos o e-mail como subject.
                 */
                .subject(usuario.getEmail())

                /*
                 * Data e hora de emissão.
                 */
                .issuedAt(agora)

                /*
                 * Data e hora de expiração.
                 */
                .expiresAt(expiracao)

                /*
                 * Claims personalizados.
                 *
                 * São informações adicionais
                 * que poderão ser utilizadas
                 * posteriormente na autorização.
                 */
                .claim(
                        "idUsuario",
                        usuario.getIdUsuario()
                )

                .claim(
                        "perfil",
                        usuario.getPerfil().name()
                )

                .build();


        /*
         * Define o algoritmo utilizado
         * para assinar o JWT.
         */
        JwsHeader header =
                JwsHeader
                        .with(MacAlgorithm.HS256)
                        .build();


        /*
         * Junta o cabeçalho e os claims.
         */
        JwtEncoderParameters parametros =
                JwtEncoderParameters.from(
                        header,
                        claims
                );


        /*
         * Assina e gera o JWT.
         *
         * O retorno será uma String semelhante a:
         *
         * eyJhbGciOiJIUzI1NiJ9...
         */
        return jwtEncoder
                .encode(parametros)
                .getTokenValue();
    }
}