//package it.example.piao.api.configuration.security;
//
//import java.security.Key;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.NoSuchAlgorithmException;
//import java.security.interfaces.RSAPublicKey;
//import java.util.Collections;
//import java.util.List;
//
//import jakarta.annotation.PostConstruct;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
//import org.springframework.security.oauth2.core.OAuth2TokenValidator;
//import org.springframework.security.oauth2.jwt.Jwt;
//import org.springframework.security.oauth2.jwt.JwtDecoder;
//import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
//import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
//import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
//import org.springframework.security.web.SecurityFilterChain;
//
//import com.nimbusds.jose.JOSEException;
//import com.nimbusds.jose.JWSHeader;
//import com.nimbusds.jose.JWSVerifier;
//import com.nimbusds.jose.crypto.RSASSAVerifier;
//import com.nimbusds.jose.crypto.factories.DefaultJWSVerifierFactory;
//import com.nimbusds.jose.proc.SecurityContext;
//import com.nimbusds.jose.util.Base64URL;
//import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
//import com.nimbusds.jwt.proc.DefaultJWTProcessor;
//
//import it.example.piao.api.model.properties.JwtProperties;
//
//@Configuration
//@ConditionalOnProperty(prefix = "security", name = "enable.jwt.configuration", havingValue = "true")
//@EnableWebSecurity
////Enable jsr250 annotations like @RolesAllowed and @PermitAll
//@EnableMethodSecurity(jsr250Enabled = true)
//public class SecurityConfig {
//
//    private RSAPublicKey key;
//
//    @PostConstruct
//    public void generateTemporaryPublicKey() throws NoSuchAlgorithmException {
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//        keyGen.initialize(2048);
//        KeyPair keyPair = keyGen.generateKeyPair();
//        this.key = (RSAPublicKey) keyPair.getPublic();
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http, JwtProperties jwtProperties) throws Exception {
//
//        String[] matchers = jwtProperties.getUnauthenticatedMatcher().stream().toArray(size -> new String[size]);
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers(matchers)
//                            .permitAll()
//                        .anyRequest()
//                            .authenticated())
//                .oauth2ResourceServer(oauth2 -> oauth2
//                        .jwt(jwt -> jwt
//                                .jwtAuthenticationConverter(jwtAuthenticationConverter())));
//
//        return http.build();
//    }
//
//    @Bean
//    public JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
//        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
//
//        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
//        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
//        return jwtAuthenticationConverter;
//    }
//
//    @Bean
//    @ConditionalOnProperty(prefix = "security", name = "skip.jwt.verification", havingValue = "true")
//    public JwtDecoder jwtDecoder() {
//
//        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
//        jwtProcessor.setJWSVerifierFactory(new CustomJWSVerifierFactory());
//
//        final NimbusJwtDecoder decoder = NimbusJwtDecoder
//                                            .withPublicKey(this.key)
//                                            .jwtProcessorCustomizer(processor -> processor.setJWSVerifierFactory(new CustomJWSVerifierFactory()))
//                                            .build();
//        decoder.setJwtValidator(tokenValidator());
//        return decoder;
//    }
//
//    public OAuth2TokenValidator<Jwt> tokenValidator() {
//        final List<OAuth2TokenValidator<Jwt>> validators = Collections.emptyList();
//        return new DelegatingOAuth2TokenValidator<>(validators);
//    }
//
//    class CustomJWSVerifierFactory extends DefaultJWSVerifierFactory {
//    	@Override
//	    public JWSVerifier createJWSVerifier(final JWSHeader header, final Key key) throws JOSEException {
//                RSAPublicKey rsaPublicKey = (RSAPublicKey)key;
//                return new CustomRSASSAVerifier(rsaPublicKey);
//        }
//    }
//
//    class CustomRSASSAVerifier extends RSASSAVerifier {
//        public CustomRSASSAVerifier(RSAPublicKey key) throws JOSEException {
//            super(key);
//        }
//
//        @Override
//	    public boolean verify(final JWSHeader header, final byte[] signedContent, final Base64URL signature)
//		throws JOSEException {
//            return true;
//        }
//    }
//
//}
