/*package it.example.piao.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AutenticazioneService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JdbcTemplate autenticazioneJdbcTemplate;
    private final String clientId;

    public AutenticazioneService(
        WebClient.Builder builder,
        @Value("${autenticazione.base-url}") String baseUrl,
        @Value("${autenticazione.client-id}") String clientId,
        @Qualifier("autenticazioneJdbcTemplate") JdbcTemplate autenticazioneJdbcTemplate
    ) {
        this.clientId = clientId;
        this.webClient = builder.baseUrl(baseUrl + clientId).build();
        this.autenticazioneJdbcTemplate = autenticazioneJdbcTemplate;
    }

    public Map<String, Object> refreshToken(String refreshToken, String codiceFiscale) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> user = autenticazioneJdbcTemplate.queryForMap(
                "SELECT NAME, SURNAME, EMAIL FROM SEMPLIFICAZIONE_ANAG.DFP_USER WHERE CODICE_FISCALE = ?",
                codiceFiscale
            );
            result.put("nome", user.get("NAME"));
            result.put("cognome", user.get("SURNAME"));
            result.put("email", user.get("EMAIL"));
        } catch (EmptyResultDataAccessException e) {
            result.put("nome", null);
            result.put("cognome", null);
            result.put("email", null);
        }

        String body = "grant_type=refresh_token" +
            "&scope=profile" +
            "&refresh_token=" + refreshToken +
            "&client_id=" + clientId;

        try {
            Map<String, Object> tokenResponse = webClient.post()
                .uri("/access_token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            result.putAll(tokenResponse);
            return result;

        } catch (WebClientResponseException ex) {
            Map<String, Object> errorMap = new HashMap<>();
            try {
                Map<String, Object> errorBody = objectMapper.readValue(ex.getResponseBodyAsString(), Map.class);
                errorMap.put("status", ex.getRawStatusCode());
                errorMap.put("error_description", errorBody.get("error_description"));
            } catch (Exception e) {
                errorMap.put("status", ex.getRawStatusCode());
                errorMap.put("error_description", ex.getResponseBodyAsString());
            }
            return errorMap;
        }
    }

    public Map<String, Object> revokeToken(String refreshToken) {
        Map<String, Object> result = new HashMap<>();

        String body = "client_id=" + clientId +
            "&token=" + refreshToken ;
            //"&token_type_hint=refresh_token";

        try {
            //  Revoca token
            Map<String, Object> response = webClient.post()
                .uri("/token/revoke")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || response.isEmpty()) {
                result.put("status", 200);
                result.put("message", "Token revocato correttamente");
            } else {
                result.putAll(response);
            }

            return result;

        } catch (WebClientResponseException ex) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", ex.getRawStatusCode());
            try {
                Map<String, Object> errorBody = objectMapper.readValue(ex.getResponseBodyAsString(), Map.class);
                errorMap.put("error_description", errorBody.getOrDefault("error_description", ex.getMessage()));
            } catch (Exception e) {
                errorMap.put("error_description", ex.getResponseBodyAsString());
            }
            return errorMap;
        }
    }



}
*/
