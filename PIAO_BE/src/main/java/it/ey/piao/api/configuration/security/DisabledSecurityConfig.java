package it.ey.piao.api.configuration.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(prefix = "security", name = "enable.jwt.configuration", havingValue = "false")
public class DisabledSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
