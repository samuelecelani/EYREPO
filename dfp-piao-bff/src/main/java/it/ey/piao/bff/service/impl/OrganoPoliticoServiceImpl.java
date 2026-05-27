package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MetadatoDTO;
import it.ey.dto.OrganoPoliticoDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IOrganoPoliticoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class OrganoPoliticoServiceImpl implements IOrganoPoliticoService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OrganoPoliticoServiceImpl.class);

    public OrganoPoliticoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API; // uguale al tuo IntegrationTeamServiceImpl
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione) {

        log.info("Richiesta cancellazione OrganoPolitico con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // Endpoint BE: DELETE /organo-politico/{id}
        StringBuilder url = new StringBuilder("/organo-politico/" + id + "?");

        if (campiModificati != null && !campiModificati.isBlank()) {
            url.append("campiModificati=")
                .append(URLEncoder.encode(campiModificati, StandardCharsets.UTF_8))
                .append("&");
        }

        if (idPiao != null) {
            url.append("idPiao=").append(idPiao).append("&");
        }

        if (testoSezione != null && !testoSezione.isBlank()) {
            url.append("testoSezione=")
                .append(URLEncoder.encode(testoSezione, StandardCharsets.UTF_8))
                .append("&");
        }

        // Rimuove eventuale '&' o '?' finale
        if (url.charAt(url.length() - 1) == '&' || url.charAt(url.length() - 1) == '?') {
            url.deleteCharAt(url.length() - 1);
        }

        return webClientService
            .deleteWithExchange(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
            )
            .defaultIfEmpty(new GenericResponseDTO<>())
            .map(apiResponse -> {

                if (apiResponse != null && apiResponse.getError() != null) {

                    MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                        .value(OrganoPoliticoDTO.class.getSimpleName())
                        .idFK(id)
                        .idPiao(idPiao)
                        .build();

                    apiResponse.setMetadato(List.of(metadato));
                    return apiResponse;
                }

                GenericResponseDTO<Void> successResponse = new GenericResponseDTO<>();
                successResponse.setStatus(new Status(true));
                return successResponse;
            })
            .onErrorResume(e -> {

                log.error("Errore cancellazione OrganoPolitico id={}: {}", id, e.getMessage(), e);

                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status(false));

                Error err = Error.builder()
                    .messageError(e.getMessage())
                    .errorCode("ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE")
                    .build();

                if (e instanceof WebClientResponseException webEx) {
                    try {
                        GenericResponseDTO<Void> errorResponse = webEx.getResponseBodyAs(
                            new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
                        );

                        if (errorResponse != null && errorResponse.getError() != null) {
                            err = errorResponse.getError();
                        }

                    } catch (Exception exParse) {
                        log.warn("Errore parsing body risposta OrganoPolitico", exParse);
                        err.setMessageError(webEx.getResponseBodyAsString());
                    }
                }

                finalResponse.setError(err);

                MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                    .value(OrganoPoliticoDTO.class.getSimpleName())
                    .idFK(id)
                    .idPiao(idPiao)
                    .build();

                finalResponse.setMetadato(List.of(metadato));

                return Mono.just(finalResponse);
            });
    }
}
