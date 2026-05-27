package it.ey.piao.bff.configuration.security;


import it.ey.piao.bff.filter.SessionAuthenticationWebFilter;
import it.ey.piao.bff.filter.UserAuthoritiesWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfigReactive {

    @org.springframework.beans.factory.annotation.Value("${bff.authEnabled:false}")
    private boolean authEnabled;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        SessionAuthenticationWebFilter sessionAuthenticationWebFilter,
        UserAuthoritiesWebFilter userAuthoritiesWebFilter,
        CorsConfigurationSource corsConfigurationSource,
        ExchangeAttributeSecurityContextRepository securityContextRepository) {

        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .cors(c -> c.configurationSource(corsConfigurationSource))
            .securityContextRepository(securityContextRepository)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(jsonAuthenticationEntryPoint())
            )
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll()
            );

        // Filtro di autenticazione Keycloak via sessione: solo se auth attiva
        if (authEnabled) {
            http.addFilterAt(sessionAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        }

        // Filtro per arricchire le richieste con i dati utente (sempre attivo)
        http.addFilterAfter(userAuthoritiesWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();

        // Origini consentite - includere tutte le porte usate in sviluppo
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:*",      // Tutte le porte su localhost
            "http://127.0.0.1:*"       // Anche 127.0.0.1
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Elenco COMPLETO di tutti gli header inviati dal FE (interceptor Angular + fetchEventSource)
        config.setAllowedHeaders(List.of(
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.ACCEPT,
            HttpHeaders.ORIGIN,
            HttpHeaders.CACHE_CONTROL,
            HttpHeaders.PRAGMA,
            HttpHeaders.COOKIE,        // Importante per i cookie
            "X-Fiscal-Code",           // cf.interceptor.ts
            "x-correlation-id",        // correlation-id.interceptor.ts
            "X-Correlation-Id",
            "X-Requested-With",
            "Last-Event-ID",           // fetchEventSource SSE reconnect
            "id-spinner",
            "ID-SPINNER"
        ));

        config.setExposedHeaders(List.of(
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.SET_COOKIE,    // Per esporre Set-Cookie al browser
            "X-Total-Count",
            "Last-Event-ID"
        ));

        // IMPORTANTE: per inviare cookie cross-origin
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Entry point custom che restituisce 401 JSON invece di mostrare form di login.
     * Non invia l'header WWW-Authenticate per evitare il popup del browser.
     */
    private ServerAuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return (exchange, ex) -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            // Non impostare WWW-Authenticate per evitare il popup del browser
            exchange.getResponse().getHeaders().remove(HttpHeaders.WWW_AUTHENTICATE);

            byte[] body = "{\"error\":\"Unauthorized\",\"message\":\"Autenticazione richiesta\"}".getBytes();
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body))
            );
        };
    }
}
