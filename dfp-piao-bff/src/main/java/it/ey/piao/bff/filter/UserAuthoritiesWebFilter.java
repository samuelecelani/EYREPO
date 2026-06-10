package it.ey.piao.bff.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.PaRiferimentoDTO;
import it.ey.dto.RuoloUserDTO;
import it.ey.dto.UserDTO;
import it.ey.externaldto.UserProfileDto;
import it.ey.externaldto.mapper.MapperUtenti;
import it.ey.piao.bff.filter.utils.AuthoritiesBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@Component
public class UserAuthoritiesWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(UserAuthoritiesWebFilter.class);

    private final ObjectMapper objectMapper;

    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_PROFILE = "X-User-Profile";

    private static final String API_V1_PREFIX = "/api";
    private static final List<String> SWAGGER_WHITELIST_PREFIXES = Arrays.asList(
        "/openapi-ui",
        "/swagger-ui",
        "/openapi",
        "/swagger-resources",
        "/webjars",
        "/favicon.ico",
        "/health",
        "/actuator",
        "/notification/subscribe",
        "/notification/test-emit",
        "/config/initializer",
        "/v3/api-docs",
        "/v2/api-docs",
        "/realms/",
        "/external/",
        "/public/",
        API_V1_PREFIX + "/auth/",
        API_V1_PREFIX +"/openapi-ui",
        API_V1_PREFIX +    "/swagger-ui",
        API_V1_PREFIX + "/openapi",
        API_V1_PREFIX +  "/swagger-resources",
        API_V1_PREFIX + "/webjars",
        API_V1_PREFIX +   "/favicon.ico",
        API_V1_PREFIX + "/notification/subscribe",
        API_V1_PREFIX + "/notification/test-emit",
        API_V1_PREFIX + "/health",
        API_V1_PREFIX + "/actuator"
    );

    public UserAuthoritiesWebFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 1) Bypass Swagger/OpenAPI
        if (isPathAllowed(exchange.getRequest().getPath().pathWithinApplication().value(), SWAGGER_WHITELIST_PREFIXES)) {
            return chain.filter(exchange);
        }

        // 2) Recupera userId e profilo dagli header iniettati dal gateway
        String userId = exchange.getRequest().getHeaders().getFirst(X_USER_ID);
        String profileB64 = exchange.getRequest().getHeaders().getFirst(X_USER_PROFILE);

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(profileB64)) {
            log.warn("Missing gateway headers: X-User-Id={}, X-User-Profile={}", userId != null ? "present" : "missing", profileB64 != null ? "present" : "missing");
            return unauthorized(exchange, "Missing gateway headers (X-User-Id / X-User-Profile)");
        }

        // 3) Decodifica il profilo utente dal Base64 iniettato dal gateway
        UserDTO user;
        try {
            user = decodeUserProfile(userId, profileB64);
        } catch (Exception e) {
            log.error("Failed to decode X-User-Profile: {}", e.getMessage(), e);
            return unauthorized(exchange, "Invalid X-User-Profile header");
        }

        List<GrantedAuthority> authorities = AuthoritiesBuilder.fromUser(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user, "N/A", authorities
        );

        // Per POST e PUT con body, verifica se è un'estensione di CampiTecniciDTO
        // Salta l'arricchimento per le richieste multipart (es. upload file)
        // perché il body multipart non è JSON e leggerlo qui ne causerebbe il consumo
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            MediaType contentType = exchange.getRequest().getHeaders().getContentType();
            if (contentType != null && contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA)) {
                log.debug("Richiesta multipart, skip arricchimento body con campi tecnici");
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                    .contextWrite(ReactiveRequestContextHolder.withRequest(exchange.getRequest()));
            }
            return enrichBodyWithCampiTecnici(exchange, chain, user, authentication)
                .contextWrite(ReactiveRequestContextHolder.withRequest(exchange.getRequest()));
        }

        // Per DELETE, GET e PATCH: non c'è body (o il body non estende CampiTecniciDTO),
        // i campi tecnici vengono iniettati come header HTTP
        if (method == HttpMethod.DELETE || method == HttpMethod.GET || method == HttpMethod.PATCH) {
            return enrichHeadersWithCampiTecnici(exchange, chain, user, authentication);
        }

        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            .contextWrite(ReactiveRequestContextHolder.withRequest(exchange.getRequest()));
    }

    /**
     * Decodifica l'header X-User-Profile (Base64 JSON) e costruisce un UserDTO
     * usando il mapper centralizzato {@link MapperUtenti#convertToUserDTO(UserProfileDto)}.
     */
    private UserDTO decodeUserProfile(String userId, String profileB64) throws JsonProcessingException {
        String json = new String(Base64.getDecoder().decode(profileB64), StandardCharsets.UTF_8);
        //TypeAuthirities prenderlo dal ruolo di BIP
        //qUALIFIA NEL MAPPING INTERNO CHE è AL LIVELLO DI AMMINISTRAZIONE

        log.info("UserLogged Base64 : {}", profileB64);

        UserProfileDto profile = objectMapper.readValue(json, UserProfileDto.class);
        UserDTO user = MapperUtenti.convertToUserDTO(profile);
        // Il codiceFiscale potrebbe non essere nel profilo, usiamo userId dal gateway come fallback
        if (user != null && StringUtils.isBlank(user.getFiscalCode())) {
            user.setFiscalCode(userId);
        }
        return user;
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
            if (path.contains(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Per richieste POST/PUT: legge il body, verifica se il JSON contiene campi tecnici,
     * e li valorizza con i dati dell'utente autenticato.
     * - Se l'oggetto ha un "id" → update: valorizza updatedBy, updatedTs, updatedByNameSurname, updatedByRole
     * - Se l'oggetto NON ha un "id" → creazione: valorizza createdBy, createdTs, createdByNameSurname, createdByRole
     */
    @SuppressWarnings("unchecked")
    private Mono<Void> enrichBodyWithCampiTecnici(ServerWebExchange exchange,
                                                  WebFilterChain chain,
                                                  UserDTO user,
                                                  Authentication authentication) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
            .defaultIfEmpty(exchange.getResponse().bufferFactory().wrap(new byte[0]))
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                String bodyString = new String(bytes, StandardCharsets.UTF_8);

                // Se il body è vuoto, procedi normalmente
                if (StringUtils.isBlank(bodyString)) {
                    return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                }

                try {
                    Map<String, Object> bodyMap = objectMapper.readValue(bodyString, Map.class);

                    // Arricchisci sempre il body con i campi tecnici per POST/PUT
                    // I DTO che non estendono CampiTecniciDTO ignoreranno i campi extra durante la deserializzazione
                    String nameSurname = buildNameSurname(user);
                    String ruoloAttivo = extractRuoloAttivo(user);

                    boolean hasId = bodyMap.containsKey("id") && bodyMap.get("id") != null;

                    if (hasId) {
                        // UPDATE: valorizza i campi di aggiornamento
                        bodyMap.put("updatedBy", user.getFiscalCode());
                        bodyMap.put("updatedTs", LocalDate.now().toString());
                        bodyMap.put("updatedByNameSurname", nameSurname);
                        bodyMap.put("updatedByRole", ruoloAttivo);
                        log.debug("CampiTecnici UPDATE enriched - updatedBy: {}, role: {}, nameSurname: {}",
                            user.getFiscalCode(), ruoloAttivo, nameSurname);
                    } else {
                        // CREAZIONE: valorizza i campi di creazione
                        bodyMap.put("createdBy",  user.getFiscalCode());
                        bodyMap.put("createdTs", LocalDate.now().toString());
                        bodyMap.put("createdByNameSurname", nameSurname);
                        bodyMap.put("createdByRole", ruoloAttivo);
                        log.debug("CampiTecnici CREATE enriched - createdBy: {}, role: {}, nameSurname: {}",
                            user.getFiscalCode(), ruoloAttivo, nameSurname);
                    }

                    // Riscrivi il body modificato
                    String modifiedBody = objectMapper.writeValueAsString(bodyMap);
                    ServerHttpRequest mutatedRequest = getServerHttpRequest(exchange, modifiedBody);

                    ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                    return chain.filter(mutatedExchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                } catch (JsonProcessingException e) {
                    log.warn("Impossibile parsificare il body come JSON, proseguo senza arricchire i campi tecnici: {}",
                        e.getMessage());
                }

                // Se non è un DTO con campi tecnici o c'è un errore di parsing, ricostruisci il body originale
                ServerHttpRequest originalRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                        return Flux.just(buffer);
                    }
                };
                ServerWebExchange originalExchange = exchange.mutate().request(originalRequest).build();
                return chain.filter(originalExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            });
    }

    /**
     * Per richieste DELETE: non avendo body, i campi tecnici vengono iniettati come header HTTP custom.
     * Il modulo BE dovrà recuperarli dagli header per valorizzare updatedBy, updatedTs, ecc.
     *
     * Header iniettati:
     * - X-Updated-By: codice fiscale dell'utente
     * - X-Updated-Ts: data corrente (yyyy-MM-dd)
     * - X-Updated-By-Name-Surname: nome e cognome dell'utente
     * - X-Updated-By-Role: ruolo attivo dell'utente
     * - X-Testo: testo sezione (dal query param "testoSezione")
     * - X-Campi-Modificati: campi modificati (dal query param "campiModificati")
     * - X-Stato-Sezione: stato sezione (dal query param "statoSezione")
     */
    private Mono<Void> enrichHeadersWithCampiTecnici(ServerWebExchange exchange,
                                                     WebFilterChain chain,
                                                     UserDTO user,
                                                     Authentication authentication) {
        String nameSurname = buildNameSurname(user);
        String ruoloAttivo = extractRuoloAttivo(user);

        // Recupera X-Testo e X-Campi-Modificati dai query parameters del frontend
        String testoSezione = sanitizeHeaderValue(exchange.getRequest().getQueryParams().getFirst("testoSezione"));
        String campiModificati = sanitizeHeaderValue(exchange.getRequest().getQueryParams().getFirst("campiModificati"));
        String statoSezione = sanitizeHeaderValue(exchange.getRequest().getQueryParams().getFirst("statoSezione"));

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .header("X-Updated-By-Name-Surname", nameSurname)
            .header("X-Updated-By-Role", ruoloAttivo != null ? ruoloAttivo : "")
            .header("X-Fiscal-Code", user.getFiscalCode())
            .header("X-Testo", testoSezione)
            .header("X-Campi-Modificati", campiModificati)
            .header("X-Stato-Sezione", statoSezione)
            .build();

        log.debug("CampiTecnici DELETE enriched via headers - updatedBy: {}, role: {}, nameSurname: {}",
            user.getFiscalCode(), ruoloAttivo, nameSurname);

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            .contextWrite(ReactiveRequestContextHolder.withRequest(mutatedRequest));
    }

    private static ServerHttpRequest getServerHttpRequest(ServerWebExchange exchange, String modifiedBody) {
        byte[] modifiedBytes = modifiedBody.getBytes(StandardCharsets.UTF_8);

        ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(modifiedBytes);
                return Flux.just(buffer);
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.setContentLength(modifiedBytes.length);
                return headers;
            }
        };
        return mutatedRequest;
    }

    /**
     * Costruisce la stringa "Nome Cognome" dall'utente.
     */
    private String buildNameSurname(UserDTO user) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(user.getNome())) {
            sb.append(user.getNome());
        }
        if (StringUtils.isNotBlank(user.getCognome())) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(user.getCognome());
        }
        return sb.toString();
    }

    /**
     * Estrae il ruolo attivo dell'utente dalla PA attiva.
     */
    private String extractRuoloAttivo(UserDTO user) {
        if (user.getPaRiferimento() != null) {
            for (PaRiferimentoDTO pa : user.getPaRiferimento()) {
                if (pa.isAttiva() && pa.getRuoli() != null) {
                    for (RuoloUserDTO ruolo : pa.getRuoli()) {
                        if (ruolo.isRuoloAttivo()) {
                            return ruolo.getDescrizione();
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Sanitizza un valore da usare come header HTTP.
     * Rimuove i caratteri newline (\n), carriage return (\r) e altri caratteri di controllo
     * che non sono permessi negli header HTTP per prevenire HTTP Response Splitting.
     *
     * @param value il valore da sanitizzare
     * @return il valore sanitizzato, o stringa vuota se null
     */
    private String sanitizeHeaderValue(String value) {
        if (value == null) {
            return "";
        }
        // Rimuove \r, \n e altri caratteri di controllo (ASCII 0-31 e 127)
        return value.replaceAll("[\\r\\n\\x00-\\x1f\\x7f]", "_");
    }
}
