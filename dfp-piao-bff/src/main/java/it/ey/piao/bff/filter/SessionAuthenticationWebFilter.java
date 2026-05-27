package it.ey.piao.bff.filter;

import it.ey.dto.LoginSessionDTO;
import it.ey.piao.bff.cache.SpringCacheService;
import it.ey.piao.bff.httpClient.OAuth2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Filtro che autentica le richieste basandosi sul cookie SESSION_ID.
 * Recupera la sessione dalla cache, valida il JWT e imposta l'autenticazione
 * nel SecurityContext di Spring Security.
 *
 * Disattivato quando bff.authEnabled=false (auth gestita dal gateway).
 */
@Component
public class SessionAuthenticationWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SessionAuthenticationWebFilter.class);
    private static final String SESSION_COOKIE_NAME = "SESSION_ID";
    private static final String SECURITY_CONTEXT_ATTR = "SPRING_SECURITY_CONTEXT";

    @Value("${bff.authEnabled:false}")
    private boolean authEnabled;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/auth/",
        "/api/auth/",
        "/openapi-ui",
        "/swagger-ui",
        "/openapi",
        "/swagger-resources",
        "/webjars",
        "/config/",
        "/external/",
        "/health/",
        "/actuator/"
    );

    private final SpringCacheService springCacheService;
    private final CacheProperties cacheProperties;
    private final ReactiveJwtDecoder jwtDecoder;
    private final OAuth2Service oAuth2Service;

    public SessionAuthenticationWebFilter(SpringCacheService springCacheService,
                                          CacheProperties cacheProperties,
                                          ReactiveJwtDecoder jwtDecoder,
                                          OAuth2Service oAuth2Service) {
        this.springCacheService = springCacheService;
        this.cacheProperties = cacheProperties;
        this.jwtDecoder = jwtDecoder;
        this.oAuth2Service = oAuth2Service;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        // Auth disattivata: il gateway gestisce l'autenticazione
        if (!authEnabled) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getPath().pathWithinApplication().value();

        // LOG IMMEDIATO per confermare che il filtro viene eseguito
        log.info(">>>>> SessionAuthenticationWebFilter INVOKED for path: {} <<<<<", path);

        // Skip per OPTIONS
        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            log.info("Skipping OPTIONS request");
            return chain.filter(exchange);
        }

        // Skip per path pubblici
        if (isPublicPath(path)) {
            log.info("Skipping public path: {}", path);
            return chain.filter(exchange);
        }

        // Log di tutti i cookie ricevuti
        log.info("=== SessionAuthenticationWebFilter ===");
        log.info("Path: {}", path);
        log.info("Request URI: {}", exchange.getRequest().getURI());
        log.info("Origin header: {}", exchange.getRequest().getHeaders().getOrigin());
        log.info("Cookie header raw: {}", exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE));
        log.info("Cookies parsed: {}", exchange.getRequest().getCookies().keySet());

        // Recupera il cookie SESSION_ID
        HttpCookie sessionCookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE_NAME);

        if (sessionCookie == null) {
            log.warn("No SESSION_ID cookie found for path: {}. Available cookies: {}", path, exchange.getRequest().getCookies().keySet());
            return unauthorized(exchange, "Session cookie not found");
        }

        if (sessionCookie.getValue() == null || sessionCookie.getValue().isBlank()) {
            log.warn("SESSION_ID cookie is empty for path: {}", path);
            return unauthorized(exchange, "Session cookie is empty");
        }

        String sessionId = sessionCookie.getValue();
        log.info("Processing session {} for path: {}", sessionId, path);

        // Recupera la sessione dalla cache
        LoginSessionDTO loginSession = springCacheService.get(
            cacheProperties.getCacheNames().getFirst(),
            sessionId,
            LoginSessionDTO.class
        );

        if (loginSession == null) {
            log.warn("❌ Session NOT FOUND in cache for sessionId: {} - Cache name: {}",
                sessionId, cacheProperties.getCacheNames().getFirst());
            return unauthorized(exchange, "Session not found in cache");
        }

        log.info("✓ Session FOUND in cache - isAuthenticated: {}, hasToken: {}, hasAccessToken: {}",
            loginSession.isAuthenticated(),
            loginSession.getToken() != null,
            loginSession.getToken() != null && loginSession.getToken().getAccessToken() != null);

        if (!loginSession.isAuthenticated()) {
            log.warn("❌ Session NOT AUTHENTICATED for sessionId: {}", sessionId);
            return unauthorized(exchange, "Session not authenticated");
        }

        if (loginSession.getToken() == null || loginSession.getToken().getAccessToken() == null) {
            log.warn("❌ No ACCESS TOKEN in session for sessionId: {}", sessionId);
            return unauthorized(exchange, "No access token in session");
        }

        String accessToken = loginSession.getToken().getAccessToken();
        log.info("✓ Access token found, length: {}, starts with: {}...",
            accessToken.length(),
            accessToken.substring(0, Math.min(50, accessToken.length())));

        // Decodifica e valida il JWT
        log.info("Attempting to decode JWT for sessionId: {}", sessionId);
        return jwtDecoder.decode(accessToken)
            .flatMap(jwt -> {
                log.info("✓ JWT decoded successfully for sessionId: {}, subject: {}", sessionId, jwt.getSubject());
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

                // Salva il context come attributo dell'exchange per il repository custom
                exchange.getAttributes().put(SECURITY_CONTEXT_ATTR, securityContext);
                log.info("Authentication saved in exchange attributes for sessionId: {}", sessionId);

                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
            })
            .onErrorResume(JwtException.class, e -> {
                log.info("⚠ Access token expired/invalid for sessionId: {}, error: {}, attempting refresh...",
                    sessionId, e.getMessage());
                return refreshTokenAndAuthenticate(exchange, chain, sessionId, loginSession);
            })
            .onErrorResume(e -> {
                log.error("❌ Unexpected error processing authentication for sessionId {}: {} - {}",
                    sessionId, e.getClass().getSimpleName(), e.getMessage());
                return unauthorized(exchange, "Authentication error: " + e.getMessage());
            });
    }

    /**
     * Tenta di fare il refresh del token e prosegue con l'autenticazione.
     */
    private Mono<Void> refreshTokenAndAuthenticate(ServerWebExchange exchange, WebFilterChain chain,
                                                    String sessionId, LoginSessionDTO loginSession) {
        String refreshToken = loginSession.getToken().getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            log.warn("No refresh token available for sessionId: {}", sessionId);
            return chain.filter(exchange);
        }

        return oAuth2Service.refreshTokens(refreshToken)
            .flatMap(newTokens -> {
                // Aggiorna la sessione in cache con i nuovi token
                loginSession.setToken(newTokens);
                springCacheService.put(cacheProperties.getCacheNames().getFirst(), sessionId, loginSession);
                log.info("Tokens refreshed successfully for sessionId: {}", sessionId);

                // Decodifica il nuovo access token
                return jwtDecoder.decode(newTokens.getAccessToken())
                    .flatMap(jwt -> {
                        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);
                        SecurityContextImpl securityContext = new SecurityContextImpl(authentication);

                        // Salva il context come attributo dell'exchange
                        exchange.getAttributes().put(SECURITY_CONTEXT_ATTR, securityContext);

                        return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)));
                    });
            })
            .onErrorResume(e -> {
                log.error("Failed to refresh token for sessionId {}: {}", sessionId, e.getMessage());
                // Se il refresh fallisce, invalida la sessione
                springCacheService.delete(cacheProperties.getCacheNames().getFirst(), sessionId);
                return unauthorized(exchange, "Token refresh failed");
            });
    }

    private boolean isPublicPath(String path) {
        if (path == null) return false;
        for (String publicPath : PUBLIC_PATHS) {
            if (path.contains(publicPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Restituisce una risposta 401 Unauthorized con un messaggio JSON.
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        log.warn("Returning 401 Unauthorized: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}
