package it.ey.piao.bff.filter;

import it.ey.dto.UserDTO;
import it.ey.piao.bff.filter.utils.AuthoritiesBuilder;
import it.ey.piao.bff.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
public class UserAuthoritiesWebFilter implements WebFilter {

    private final IUserService userService;

    private static final List<String> SWAGGER_WHITELIST_PREFIXES = Arrays.asList(
        "/openapi-ui",
        "/swagger-ui",
        "/openapi",
        "/swagger-resources",
        "/webjars",
        "/favicon.ico",
        "/config/initializer"
        // es: /v3/api-docs, /v3/api-docs/**
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
            .onErrorResume(ex -> unauthorized(exchange, "Unauthorized"));
    }


    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        String json = "{\"error\":\"" + message + "\"}";
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
