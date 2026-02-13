package it.ey.piao.bff.service.impl;


import it.ey.dto.CallbackDTO;
import it.ey.dto.LoginDataDTO;
import it.ey.dto.LoginRequestDataDTO;
import it.ey.dto.LoginSessionDTO;
import it.ey.piao.bff.cache.SpringCacheService;
import it.ey.piao.bff.httpClient.OAuth2Service;
import it.ey.piao.bff.property.PropertyAuthentication;
import it.ey.utils.PKCEUtils;
import it.ey.utils.TokenUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.security.auth.login.LoginException;
import java.time.Instant;
import java.util.UUID;
@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final PKCEUtils pkceUtils;
    private final OAuth2Service oAuth2Service;
    // private final CleanupService cleanupService;
    private final SpringCacheService springCacheService;
    private final CacheProperties cacheProperties;

    private final TokenUtils tokenUtils;
    private final PropertyAuthentication props;

    public AuthenticationService(OAuth2Service oAuth2Service, SpringCacheService springCacheService, CacheProperties cacheProperties, PropertyAuthentication props) {
        this.springCacheService = springCacheService;
        this.cacheProperties = cacheProperties;
        this.pkceUtils = new PKCEUtils();
        this.oAuth2Service = oAuth2Service;
        //this.cleanupService = cleanupService;
        this.tokenUtils = new TokenUtils();
        this.props = props;
    }

    public LoginDataDTO login(String originUri, String redirectUri) {
        String state = UUID.randomUUID().toString();
        String verifier = pkceUtils.createCodeVerifier();
        String challenge = pkceUtils.createChallenge(verifier);

        String uri = UriComponentsBuilder.fromUriString(props.getOauth2Url() + "/protocol/openid-connect/auth")
            .queryParam("response_type", "code")
            .queryParam("client_id", props.getClientId())
            .queryParam("redirect_uri", redirectUri)
            .queryParam("scope", String.join(" ", props.getScopes()))
            .queryParam("state", state)
            .queryParam("code_challenge_method", "S256")
            .queryParam("code_challenge", challenge)
            .build()
            .toUriString();

        String sessionId = UUID.randomUUID().toString();

        LoginRequestDataDTO loginRequestData = new LoginRequestDataDTO(state, originUri, redirectUri, verifier);
        LoginSessionDTO loginSession = new LoginSessionDTO(false, loginRequestData, null);
        // Salvataggio nella cache centralizzata

        //TODO:ripulire dopo i test con FE e gestire multiCacheName
        springCacheService.put(cacheProperties.getCacheNames().getFirst(), state, loginSession);
        springCacheService.put(cacheProperties.getCacheNames().getFirst(), sessionId, loginSession);

        return new LoginDataDTO(sessionId, uri);
    }

    public Mono<CallbackDTO> callback(String authorizationCode, String sessionId, String state) {
        log.debug("Retrieving login session from cache.");
        LoginSessionDTO loginSession = springCacheService.get(cacheProperties.getCacheNames().getFirst(),sessionId,LoginSessionDTO.class);
        if (loginSession == null || loginSession.getLoginRequestData() == null) {
            return Mono.error(new LoginException("Login session not found in cache."));
        }

        LoginRequestDataDTO loginRequestData = loginSession.getLoginRequestData();

        if (!loginRequestData.getState().equals(state)) {
            return Mono.error(new LoginException("State mismatch between cookie and Keycloak response."));
        }

        return oAuth2Service.tokensByAuthorizationCode(
            authorizationCode,
            loginRequestData.getRedirectUri(),
            loginRequestData.getCodeVerifier()

        )

            .doOnError(e -> log.error("Errore nello scambio token: {}", e.getMessage(), e))
            .flatMap(
            tokensDTO -> {

            loginSession.setAuthenticated(true);
            loginSession.setLoginRequestData(null); // opzionale: puoi rimuovere i dati di richiesta
            loginSession.setToken(tokensDTO);

            Instant refreshTokenExpiry = tokenUtils.getRefreshTokenExpiry(tokensDTO.getRefreshExpiresIn());

            springCacheService.put(cacheProperties.getCacheNames().getFirst(), sessionId, loginSession);

            log.debug("Tokens stored in cache.");

            return Mono.just(new CallbackDTO(sessionId, loginRequestData.getOriginUri(), refreshTokenExpiry));
        });
    }
    public Mono<String> logout( String sessionId, String redirectUri) {
        // Recupera la sessione dalla cache
        LoginSessionDTO loginSession = springCacheService.get(cacheProperties.getCacheNames().getFirst(),sessionId,LoginSessionDTO.class);
        if (loginSession == null || loginSession.getToken() == null) {
            return Mono.error(new LoginException("Sessione non trovata o già invalidata."));
        }

        // Rimuovi dalla cache
        springCacheService.delete(cacheProperties.getCacheNames().getFirst(), sessionId);


        // Costruisci URL di logout Keycloak
        String logoutUri = UriComponentsBuilder.fromUriString(props.getOauth2Url() + "/protocol/openid-connect/logout")
            .queryParam("id_token_hint", loginSession.getToken().getIdToken())
            .queryParam("post_logout_redirect_uri", redirectUri)
            .build()
            .toUriString();

        return Mono.just(logoutUri);
    }
//    public Mono<String> logout(String baseUrl, @Nullable String sessionId, String redirectUri) {
//        UriBuilder logoutUri = UriBuilder.fromUri(props.getOAuth2Url() + "/protocol/openid-connect/logout");
//
//        if (sessionId == null) {
//            return Mono.just(logoutUri.build().toString());
//        }
//
//        log.debug("Ricerca dei token dalla cache.", "");
//        return profiliClient.getDatiSessione(sessionId)
//            .flatMap(datiSessione -> {
//                TokensDTO tokens = (datiSessione != null) ? datiSessione.tokens() : null;
//
//                if (tokens != null) {
//                    logoutUri
//                        .queryParam("id_token_hint", tokens.idToken())
//                        .queryParam("post_logout_redirect_uri", redirectUri);
//
//                    log.debug("Rimozione dei token e del profilo dalla cache.", "");
//                    profiliClient.deleteDatiSessione(sessionId)
//                        .subscribe()
//                        .with(response -> log.debug("Eliminato i token dalla cache", ""));
//
//                    log.debug("Cleanup dei vari moduli.", "");
//                    cleanupService.cleanup(baseUrl, tokens.accessToken())
//                        .subscribe()
//                        .with(response -> log.debug("Cleanup completato", ""));
//                } else {
//                    log.warn("Token non trovati nella cache. Il refresh_token potrebbe essere scaduto. Nessun redirect post logout.", "");
//                }
//
//                return Mono.just(logoutUri.build().toString());
//            });
//    }
}
