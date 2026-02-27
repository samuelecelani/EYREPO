package it.sogei.dfp.gateway.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CorsSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@EnableWebFluxSecurity
public class SecurityConfig {

	@Value("${gateway.authEnabled:true}")
	private boolean authEnabled;

	@Value("${gateway.cors.allowedOrigin:''}")
	private String allowedOrigin;

	@Value("${gateway.cors.enabled:false}")
	private boolean corsEnabled;

	private final JwtAuthConverter jwtAuthConverter;

	public SecurityConfig(JwtAuthConverter jwtAuthConverter) {
		this.jwtAuthConverter = jwtAuthConverter;
	}

    @Bean
    SecurityWebFilterChain springWebFilterChain(ServerHttpSecurity http) {

        http.csrf(CsrfSpec::disable);

        if (corsEnabled) {
            http.cors(Customizer.withDefaults());
        } else {
            http.cors(CorsSpec::disable);
        }

        if (authEnabled) {
            http.authorizeExchange((authorize) -> authorize
            		.pathMatchers("/actuator/**").permitAll()
                    .anyExchange().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
        } else {
            http.authorizeExchange((authorize) -> authorize.anyExchange().permitAll());
        }
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList(allowedOrigin));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowedMethods(Arrays.stream(HttpMethod.values()).map(HttpMethod::name).toList());
        config.setExposedHeaders(Arrays.asList(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.CONTENT_DISPOSITION,
                HttpHeaders.CONTENT_TYPE));
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
