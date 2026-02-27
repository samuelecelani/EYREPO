package it.ey.piao.bff.filter;

import it.ey.dto.UserDTO;
import it.ey.piao.bff.filter.utils.AuthoritiesBuilder;
import it.ey.piao.bff.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class UserAuthoritiesWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(UserAuthoritiesWebFilter.class);

    private final IUserService userService;
    private static final String API_V1_PREFIX = "/api/v1";
    private static final List<String> SWAGGER_WHITELIST_PREFIXES = Arrays.asList(
        "/openapi-ui",
        "/swagger-ui",
        "/openapi",
        "/swagger-resources",
        "/webjars",
        "/favicon.ico",
        "/notification/",
        API_V1_PREFIX + "/config/initializer",
        API_V1_PREFIX +"/openapi-ui",
        API_V1_PREFIX +    "/swagger-ui",
        API_V1_PREFIX + "/openapi",
        API_V1_PREFIX +  "/swagger-resources",
        API_V1_PREFIX + "/webjars",
        API_V1_PREFIX +   "/favicon.ico",
        API_V1_PREFIX + "/config/initializer",
        API_V1_PREFIX + "/notification/"
    );

    public UserAuthoritiesWebFilter(IUserService userService) {
        this.userService = userService;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Recupera il codice fiscale dall'header
        String fiscalCode = exchange.getRequest().getHeaders().getFirst("X-Fiscal-Code");


        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 1) Bypass Swagger/OpenAPI
        if (isPathAllowed(exchange.getRequest().getPath().pathWithinApplication().value(), SWAGGER_WHITELIST_PREFIXES)) {
            return chain.filter(exchange);
        }


        if (StringUtils.isBlank(fiscalCode)) {
            return unauthorized(exchange, "Missing or invalid X-Fiscal-Code header");
        }
        //TODO: Appena disponibile il servizio adeguare la logica qui

        // Chiama il service passando il codice fiscale
        return userService.getUserbyToken()
            .flatMap(response -> {
                if (response == null || response.getData() == null) {
                    log.warn("User not found for fiscal code: {}", fiscalCode);
                    return unauthorized(exchange, "User not found");
                }

                UserDTO user = response.getData();
                List<GrantedAuthority> authorities = AuthoritiesBuilder.fromUser(user);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, "N/A", authorities
                );

                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            })
            .onErrorResume(ex -> {
                // Log dell'errore per debugging
                log.error("Error in UserAuthoritiesWebFilter: {}", ex.getMessage(), ex);

                // Gestione solo per errori di autenticazione - gli altri li lascia al GlobalErrorHandler
                if (ex instanceof WebClientResponseException webClientEx) {
                    HttpStatus status = HttpStatus.resolve(webClientEx.getStatusCode().value());

                    // Solo 401/403 vengono gestiti qui
                    if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                        return unauthorized(exchange, "Unauthorized");
                    }
                }

                // Per tutti gli altri errori, ritorna l'errore originale che verrà gestito dal GlobalErrorHandler
                return Mono.error(ex);
            });
    }


    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        return handleError(exchange, message);
    }

    private Mono<Void> handleError(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        String json = String.format("{\"error\":\"%s\",\"status\":%d}",
            message.replace("\"", "\\\""), HttpStatus.UNAUTHORIZED.value());
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(bytes)));
    }

    private boolean isPathAllowed(String path, List<String> paths) {
        if (path == null) return false;
        for (String prefix : paths) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
