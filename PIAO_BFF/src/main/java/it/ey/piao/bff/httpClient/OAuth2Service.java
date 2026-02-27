package it.ey.piao.bff.httpClient;

import it.ey.dto.TokenDTO;
import it.ey.piao.bff.property.PropertyAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class OAuth2Service {

    private final WebClient webClient;
    private final PropertyAuthentication props;
    private final static Logger log = LoggerFactory.getLogger(OAuth2Service.class);

    public OAuth2Service(WebClient.Builder builder, PropertyAuthentication props) {
        this.webClient = builder.baseUrl(props.getOauth2Url()).build();
        this.props = props;
    }


    public Mono<TokenDTO> tokensByAuthorizationCode(String code, String redirectUri, String codeVerifier) {
        return webClient.post()
            .uri(props.getOauth2Url() + "/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                .with("code", code)
                .with("redirect_uri", redirectUri)
                .with("client_id", props.getClientId())
                .with("code_verifier", codeVerifier)
                //.with("client_secret", "ed9bqhaHeasgQkEF2OJrL9h3Ef5nDmGa")
            )
            .retrieve()
            .onStatus(status -> status.isError(), clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .doOnNext(body -> log.error("Errore Keycloak: {}", body))
                    .then(Mono.error(new RuntimeException("Errore token endpoint")))
            )
            .bodyToMono(TokenDTO.class)
            .timeout(Duration.ofSeconds(10))
            .doOnSubscribe(sub -> log.info("Chiamata token endpoint iniziata"))
            .doOnSuccess(token -> log.info("Token ricevuto: {}", token))
            .doOnError(err -> log.error("Errore WebClient: {}", err.getMessage(), err));

    }

    public Mono<TokenDTO> refreshTokens(String refreshToken) {
        return webClient.post()
            .uri(props.getOauth2Url() + "/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", refreshToken)
                    .with("client_id", props.getClientId())
                // Se il client richiede secret, aggiungi:
                // .with("client_secret", props.getClientSecret())
            )
            .retrieve()
            .onStatus(status -> status.isError(), clientResponse ->
                clientResponse.bodyToMono(String.class)
                    .doOnNext(body -> log.error("Errore Keycloak refresh: {}", body))
                    .then(Mono.error(new RuntimeException("Errore refresh token endpoint")))
            )
            .bodyToMono(TokenDTO.class)
            .timeout(Duration.ofSeconds(10))
            .doOnSubscribe(sub -> log.info("Chiamata refresh token iniziata"))
            .doOnSuccess(token -> log.info("Nuovi token ricevuti: {}", token))
            .doOnError(err -> log.error("Errore WebClient refresh: {}", err.getMessage(), err));
    }

}
