//package it.example.piao.bff.configuration.security;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import it.example.dto.GenericResponseDTO;
//import it.example.dto.Status;
//import it.example.piao.bff.filter.SessionAuthenticationFilter;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.core.convert.converter.Converter;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.JwtDecoders;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.List;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//@Configuration
//@EnableWebSecurity
////Enable jsr250 annotations like @RolesAllowed and @PermitAll
////@EnableMethodSecurity(jsr250Enabled = true)
//
//@EnableMethodSecurity(prePostEnabled = true)
//public class SecurityConfig {
//
////    private RSAPublicKey key;
////
////    @PostConstruct
////    public void generateTemporaryPublicKey() throws NoSuchAlgorithmException {
////        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
////        keyGen.initialize(2048);
////        KeyPair keyPair = keyGen.generateKeyPair();
////        this.key = (RSAPublicKey) keyPair.getPublic();
////    }
//@Value("${external.keykloack.uri}")
//private String locationDecoder;
//
//    @Bean
//    @Order(1)
//    SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
//        http.cors(withDefaults())
//            .csrf(AbstractHttpConfigurer::disable)
//            .securityMatcher(
//                "/auth/**",
//                "/swagger-ui/**",
//                "/v3/api-docs/**",
//                "/swagger-resources/**",
//                "/swagger-ui.html",
//                "/webjars/**",
//                "/openapi-ui/**",
//                "/openapi/**",
//                "/test/token"
//            )
//            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
//            .csrf(AbstractHttpConfigurer::disable)
//            .sessionManagement(AbstractHttpConfigurer::disable);
//        return http.build();
//    }
//
//    @Bean
//    @Order(2)
//    SecurityFilterChain protectedChain(HttpSecurity http,
//                                       SessionAuthenticationFilter sessionAuthenticationFilter,
//                                       ObjectMapper objectMapper) throws Exception {
//        http
//            .cors(withDefaults())
//            .csrf(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
//            .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//            .securityContext(securityContext -> securityContext.requireExplicitSave(false))
//            .sessionManagement(AbstractHttpConfigurer::disable)
//            .exceptionHandling(ex -> ex
//                .authenticationEntryPoint((req, res, excep) -> {
//                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
//                    res.setContentType("application/json");
//                    GenericResponseDTO<Void> response = new GenericResponseDTO<>();
//                    response.setStatus(Status.builder().isSuccess(false).build());
//                    response.setError(it.example.dto.Error.builder()
//                        .messageError("Autenticazione richiesta").build());
//                    res.getWriter().write(objectMapper.writeValueAsString(response));
//                })
//                .accessDeniedHandler((req, res, excep) -> {
//                    res.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
//                    res.setContentType("application/json");
//                    GenericResponseDTO<Void> response = new GenericResponseDTO<>();
//                    response.setStatus(Status.builder().isSuccess(false).build());
//                    response.setError(it.example.dto.Error.builder()
//                        .messageError("Accesso negato").build());
//                    res.getWriter().write(objectMapper.writeValueAsString(response));
//                })
//            );
//
//        return http.build();
//    }
//
//
//
//    @Bean
//    public JwtDecoder jwtDecoder() {
//        return JwtDecoders.fromIssuerLocation(locationDecoder);
//    }
//
//    @Bean
//    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
//        return converter;
//    }
//
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowedOrigins(List.of("http://localhost:4200"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", config);
//        return source;
//    }
//
//
//
//}
//
//
//
//
//
