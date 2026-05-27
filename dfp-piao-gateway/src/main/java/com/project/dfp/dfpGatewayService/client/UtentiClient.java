package com.project.dfp.dfpGatewayService.client;

import com.project.dfp.dfpGatewayService.dto.UserProfileDto;
import com.project.dfp.dfpGatewayService.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UtentiClient {

    private static final Logger logger = LogManager.getLogger(UtentiClient.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${backend.piao.uri.utenti_public}")
    private String UTENTI_URL;

    @Value("${gateway.filter.header.useridKey}")
    private String X_USER_ID;

    @Value("${gateway.filter.header.amministrazioneidKey}")
    private String AMMINISTRAZIONE_ID_KEY;

    public Mono<UserProfileDto> retrieveProfile(String username, String amministrazioneId) {
        return webClientBuilder
                .baseUrl(UTENTI_URL.replaceAll("/$", "") + "/utenti/public/user")
                .defaultHeader(X_USER_ID, username)
                .defaultHeader(AMMINISTRAZIONE_ID_KEY, amministrazioneId)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/retrieve_profiles")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserProfileDto>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse.getStatus().isErrorStatus() || apiResponse.getData() == null) {
                        logger.error("/retrieve_profile API Error for username={}, amministrazioneId={}", username, amministrazioneId);
                        return Mono.empty();
                    }

                    logger.info("Retrieved profile for username={}, amministrazioneId={}", username, amministrazioneId);
                    return Mono.just(apiResponse.getData());
                })
                .doOnError(error ->
                        logger.error("Error retrieving the profile for username={}, amministrazioneId={}: {}",
                                username, amministrazioneId, error.getMessage(), error));
    }
}