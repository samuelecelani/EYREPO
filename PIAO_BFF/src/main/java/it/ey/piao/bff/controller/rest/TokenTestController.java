package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
//Rest Controller di test per recuperare un token da keykloack
//Serve solo per debug e test del BE, nel nostro tamplate il token viene messo in header da FE
@ApiV1Controller("/test/token")
public class TokenTestController {

    @GetMapping
    public ResponseEntity<String> getToken() {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", "piaoClient");
        body.add("client_secret", "ed9bqhaHeasgQkEF2OJrL9h3Ef5nDmGa");
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String tokenUrl = "http://localhost:8098/realms/PIAO/protocol/openid-connect/token";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            String token = (String) response.getBody().get("access_token");
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Errore nel recupero del token: " + e.getMessage());
        }
    }
}
