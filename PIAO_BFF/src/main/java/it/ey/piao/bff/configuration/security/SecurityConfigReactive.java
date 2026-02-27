package it.ey.piao.bff.configuration.security;


import it.ey.piao.bff.filter.UserAuthoritiesWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfigReactive {




    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        UserAuthoritiesWebFilter userAuthoritiesWebFilter,
        CorsConfigurationSource corsConfigurationSource) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(c -> c.configurationSource(corsConfigurationSource))
            .authorizeExchange(exchanges -> exchanges
                // 🔑 Preflight OPTIONS sempre permesso
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // swagger/openapi liberi
                .pathMatchers("/openapi-ui/**", "/swagger-ui/**", "/openapi/**", "/swagger-resources/**", "/webjars/**").permitAll()
                .anyExchange().permitAll()
            )
            // Assicurati che il tuo filtro non blocchi le OPTIONS
            .addFilterAt(userAuthoritiesWebFilter, SecurityWebFiltersOrder.FIRST)
            .build();
    }

//    @Bean
//    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
//
//        return http
//            .csrf(ServerHttpSecurity.CsrfSpec::disable)
//            .cors(ServerHttpSecurity.CorsSpec::disable)
//            .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
//            .build();
//    }



    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Elenco COMPLETO di tutti gli header inviati dal FE (interceptor Angular + fetchEventSource)
        // NON usare "*" con allowCredentials=true: il browser lo blocca
        config.setAllowedHeaders(List.of(
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.ACCEPT,
            HttpHeaders.ORIGIN,
            HttpHeaders.CACHE_CONTROL,
            HttpHeaders.PRAGMA,
            "X-Fiscal-Code",       // cf.interceptor.ts
            "x-correlation-id",    // correlation-id.interceptor.ts
            "X-Correlation-Id",
            "X-Requested-With",
            "Last-Event-ID",        // fetchEventSource SSE reconnect
            "id-spinner",
            "ID-SPINNER"
        ));
        config.setExposedHeaders(List.of(
            HttpHeaders.CONTENT_TYPE,
            "X-Total-Count",
            "Last-Event-ID"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    }
