package com.project.dfp.dfpGatewayService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dfp.dfpGatewayService.client.UtentiClient;
import com.project.dfp.dfpGatewayService.dto.RoleDto;
import com.project.dfp.dfpGatewayService.dto.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class JwtHeaderFilter extends AbstractGatewayFilterFactory<JwtHeaderFilter.Config> {

	@Value("${gateway.filter.header.useridKey}")
	private String USER_ID;

	@Value("${gateway.filter.header.profileKey}")
	private String USER_PROFILE;

    @Value("${gateway.filter.header.amministrazioneidKey}")
    private String AMMINISTRAZIONE_ID_KEY;

    @Value("${gateway.authEnabled:true}")
    private boolean authEnabled;

    // ── Dati mock (usati solo quando authEnabled = false) ──
    // Allineati con UserServiceImpl.getUserbyToken()
    private static final String MOCK_CODICE_FISCALE  = "RSSMRA80A01H501U";
    private static final String MOCK_NOME            = "Samuele";
    private static final String MOCK_COGNOME         = "Celani";
    private static final String MOCK_AMM_ID          = "1234";
    private static final String MOCK_AMM_NOME        = "Comune di Rimini";
    private static final String MOCK_AMM_EMAIL       = "emailprova@istituzione.it";
    private static final String MOCK_AMM_PHONE       = "+390541123456";
    private static final String MOCK_AMM_QUALIFICA   = "Dirigente";
    private static final String MOCK_RUOLO           = "REFERENTE";

    @Autowired
    private UtentiClient utentiClient;

    @Autowired
    private ObjectMapper objectMapper;

    public JwtHeaderFilter() {
        super(Config.class);
    }

    public static class Config {}

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> extractAndAddHeaders(exchange, chain);
    }

    public Mono<Void> extractAndAddHeaders(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        log.info(">>> JwtHeaderFilter ENTRY – authEnabled={}, path={}, headers={}",
                authEnabled, path, exchange.getRequest().getHeaders().keySet());

        // ── Auth disattivata: inietta header mock senza chiamare servizi ──
        if (!authEnabled) {
            log.info("Auth disabled – injecting mock headers. User: {} | Amm: {}", MOCK_CODICE_FISCALE, MOCK_AMM_ID);

            UserProfileDto mockProfile = UserProfileDto.builder()
                    .id("mock-id")
                    .name(MOCK_NOME)
                    .surname(MOCK_COGNOME)
                    .codiceFiscale(MOCK_CODICE_FISCALE)
                    .amministrazioni(List.of(
                            UserProfileDto.AmministrazioneDto.builder()
                                    .id(MOCK_AMM_ID)
                                    .name(MOCK_AMM_NOME)
                                    .email(MOCK_AMM_EMAIL)
                                    .phone(MOCK_AMM_PHONE)
                                    .qualifica(MOCK_AMM_QUALIFICA)
                                    .ruoli(List.of(
                                            RoleDto.builder()
                                                    .name(MOCK_RUOLO)
                                                    .build()))
                                    .build()))
                    .build();

            String profileB64 = serializeProfileToBase64(mockProfile);
            ServerWebExchange mutatedExchange = buildRequestWithHeaders(exchange, MOCK_CODICE_FISCALE, profileB64);
            return chain.filter(mutatedExchange);
        }

        // ── Auth attiva: flusso originale con JWT ──
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.defer(() -> {
                    // Rotta pubblica senza principal: passa attraverso senza header JWT
                    log.debug("No principal found (public route), forwarding without JWT headers");
                    return chain.filter(exchange).then(Mono.empty());
                }))
                .flatMap(auth -> {
                    if (!(auth.getPrincipal() instanceof Jwt jwt)) {
                        log.warn("Principal is not a JWT");

                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .headers(headers -> headers.remove(USER_ID))
                                .headers(headers -> headers.remove(USER_PROFILE))
                                .build();
                        return chain.filter(exchange.mutate().request(request).build());

                    }

                    String amministrazioneId = exchange.getRequest()
                            .getHeaders()
                            .getFirst(AMMINISTRAZIONE_ID_KEY);

                    if (amministrazioneId == null) {
                        log.warn("Missing {} header", AMMINISTRAZIONE_ID_KEY);

                        ServerHttpRequest request = exchange.getRequest().mutate()
                                .headers(headers -> headers.remove(USER_ID))
                                .headers(headers -> headers.remove(USER_PROFILE))
                                .build();
                        return chain.filter(exchange.mutate().request(request).build());

                    }

                    String codiceFiscale = jwt.getClaimAsString("nickname");
                    if (codiceFiscale == null || codiceFiscale.isEmpty()) {
                        codiceFiscale = jwt.getSubject();
                    }

                    log.info("JWT extraction. User: {} | Amm: {}", codiceFiscale, amministrazioneId);

                    String finalCodiceFiscale = codiceFiscale;
                    return utentiClient.retrieveProfile(codiceFiscale, amministrazioneId)
                            .map(this::serializeProfileToBase64)
                            .map(profileB64 -> buildRequestWithHeaders(exchange, finalCodiceFiscale, profileB64))
                            .flatMap(chain::filter)
                            .onErrorResume(e -> {
                                log.warn("Profile fetch failed {}/{}", finalCodiceFiscale, amministrazioneId);
                                return chain.filter(exchange);
                            });
                });
    }

    private String serializeProfileToBase64(Object profile) {
        try {
            String profileJson = objectMapper.writeValueAsString(profile);
            return Base64.getEncoder()
                    .encodeToString(profileJson.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Profile serialization failed: {}", e.getMessage());
            return "";
        }
    }

    private ServerWebExchange buildRequestWithHeaders(ServerWebExchange exchange,
                                                      String codiceFiscale,
                                                      String profileB64) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(USER_ID, codiceFiscale)
                .header(USER_PROFILE, profileB64)
                .header("Authorization", exchange.getRequest().getHeaders().getFirst("Authorization"))
                .build();

        log.debug("Headers injected: {}={}, {}={}({}b)",
                USER_ID, codiceFiscale,
                USER_PROFILE, "B64", profileB64.length());

        return exchange.mutate().request(mutatedRequest).build();
    }
}
