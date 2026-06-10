package it.ey.piao.bff.configuration.security;

import it.ey.dto.LoginSessionDTO;
import it.ey.dto.TokenDTO;
import it.ey.piao.bff.cache.SpringCacheService;
import it.ey.piao.bff.httpClient.OAuth2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Converte il cookie SESSION_ID in un'autenticazione JWT.
 * Recupera il token dalla cache di sessione e lo valida tramite JwtDecoder.
 * Se il token è scaduto, tenta il refresh automatico.
 */
@Component
public class SessionAuthenticationConverter implements ServerAuthenticationConverter {

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationConverter.class);
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";

    private final SpringCacheService springCacheService;
    private final CacheProperties cacheProperties;
    private final ReactiveJwtDecoder jwtDecoder;
    private final OAuth2Service oAuth2Service;

    public SessionAuthenticationConverter(SpringCacheService springCacheService,
                                          CacheProperties cacheProperties,
                                          ReactiveJwtDecoder jwtDecoder,
                                          OAuth2Service oAuth2Service) {
        this.springCacheService = springCacheService;
        this.cacheProperties = cacheProperties;
        this.jwtDecoder = jwtDecoder;
        this.oAuth2Service = oAuth2Service;
    }

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE_NAME);

        if (sessionCookie == null || sessionCookie.getValue() == null || sessionCookie.getValue().isBlank()) {
            log.debug("No SESSION_ID cookie found");
            return Mono.empty();
        }

        String sessionId = sessionCookie.getValue();
        log.debug("Processing session: {}", sessionId);

        LoginSessionDTO loginSession = springCacheService.get(
            cacheProperties.getCacheNames().getFirst(),
            sessionId,
            LoginSessionDTO.class
        );

        if (loginSession == null) {
            log.warn("Session not found in cache for sessionId: {}", sessionId);
            return Mono.empty();
        }

        if (!loginSession.isAuthenticated()) {
            log.warn("Session not authenticated for sessionId: {}", sessionId);
            return Mono.empty();
        }

        if (loginSession.getToken() == null || loginSession.getToken().getAccessToken() == null) {
            log.warn("No access token in session for sessionId: {}", sessionId);
            return Mono.empty();
        }

        String accessToken = loginSession.getToken().getAccessToken();

        return jwtDecoder.decode(accessToken)
            .map(jwt -> (Authentication) new JwtAuthenticationToken(jwt))
            .doOnSuccess(auth -> log.debug("JWT decoded successfully for sessionId: {}", sessionId))
            .onErrorResume(JwtException.class, e -> {
                log.info("Access token expired for sessionId: {}, attempting refresh...", sessionId);
                return refreshTokenAndAuthenticate(sessionId, loginSession);
            })
            .onErrorResume(e -> {
                log.error("Unexpected error decoding JWT for sessionId {}: {}", sessionId, e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Tenta di fare il refresh del token e ritorna l'autenticazione con il nuovo token.
     */
    private Mono<Authentication> refreshTokenAndAuthenticate(String sessionId, LoginSessionDTO loginSession) {
        String refreshToken = loginSession.getToken().getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("No refresh token available for sessionId: {}", sessionId);
            return Mono.empty();
        }

        return oAuth2Service.refreshTokens(refreshToken)
            .flatMap(newTokens -> {
                // Aggiorna la sessione in cache con i nuovi token
                loginSession.setToken(newTokens);
                springCacheService.put(cacheProperties.getCacheNames().getFirst(), sessionId, loginSession);
                log.info("Tokens refreshed successfully for sessionId: {}", sessionId);

                // Decodifica il nuovo access token
                return jwtDecoder.decode(newTokens.getAccessToken())
                    .map(jwt -> (Authentication) new JwtAuthenticationToken(jwt));
            })
            .doOnError(e -> log.error("Failed to refresh token for sessionId {}: {}", sessionId, e.getMessage()))
            .onErrorResume(e -> {
                // Se il refresh fallisce, invalida la sessione
                log.warn("Refresh token failed, invalidating session: {}", sessionId);
                springCacheService.delete(cacheProperties.getCacheNames().getFirst(), sessionId);
                return Mono.empty();
            });
    }
}
