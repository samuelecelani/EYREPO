package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.TypeErrorEnum;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IOVPStrategiaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class OVPStrategiaServiceImpl implements IOVPStrategiaService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);

    public OVPStrategiaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    public Mono<GenericResponseDTO<OVPStrategiaDTO>> save(OVPStrategiaDTO request, Long idOVP) {
        log.info("Salvataggio OVPStrategia per OVP con id: {}", idOVP);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia/save/" + idOVP;
        return webClientService.post(url, webServiceType, request, headers, OVPStrategiaDTO.class)
            .doOnNext(response -> log.info("OVPStrategia Salvata/Modficata: {}", response))
            .map(strategia-> {
                GenericResponseDTO<OVPStrategiaDTO> finalResponse = new GenericResponseDTO<>();
                if (strategia == null){
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica OVPStrategia");
                }
                finalResponse.setData(strategia);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica OVPStrategiaDTO {}", e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<OVPStrategiaDTO>>> findByOvpId(Long idOvp) {
        log.info("Recupero strategie per OVP con id: {}", idOvp);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia/ovp/" + idOvp;
        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<OVPStrategiaDTO>>() {})
            .doOnNext(response -> log.info("Numero strategie ricevute per OVP {}: {}", idOvp, response.size()))
            .map(strategie -> {
                GenericResponseDTO<List<OVPStrategiaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(strategie);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle strategie per OVP id={}: {}", idOvp, e.getMessage());
                GenericResponseDTO<List<OVPStrategiaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<Void>> delete(Long id, String campiModificati, Long idPiao, String testoSezione, boolean forceDelete) {

        log.info("Richiesta eliminazione OVP/Strategia con id={}, forceDelete={}", id, forceDelete);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/ovp-strategia/delete/" + id + "?");

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

        url.append("forceDelete=").append(forceDelete);

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
                        .value(OVPStrategiaDTO.class.getSimpleName())
                        .idFK(id)
                        .idPiao(idPiao)
                        .build();

                    apiResponse.setMetadato(List.of(metadato));

                    return apiResponse;
                }

                GenericResponseDTO<Void> successResponse = new GenericResponseDTO<>();
                Status status = new Status();
                status.setSuccess(true);
                successResponse.setStatus(status);

                return successResponse;
            })

            .onErrorResume(e -> {

                log.error("Errore eliminazione OVP/Strategia id={}: {}", id, e.getMessage(), e);

                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                Status status = new Status();
                status.setSuccess(false);
                finalResponse.setStatus(status);

                Error err = Error.builder()
                    .messageError(e.getMessage())
                    .errorCode("ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE")
                    .build();

                if (e instanceof WebClientResponseException webEx) {
                    try {
                        GenericResponseDTO<Void> errorResponse =
                            webEx.getResponseBodyAs(new ParameterizedTypeReference<GenericResponseDTO<Void>>() {});

                        if (errorResponse != null && errorResponse.getError() != null) {
                            err = errorResponse.getError();
                        }

                    } catch (Exception exParse) {
                        log.warn("Errore parsing body risposta OVP/Strategia", exParse);
                        err.setMessageError(webEx.getResponseBodyAsString());
                    }
                }

                finalResponse.setError(err);

                MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                    .value(OVPStrategiaDTO.class.getSimpleName())
                    .idFK(id)
                    .idPiao(idPiao)
                    .build();

                finalResponse.setMetadato(List.of(metadato));

                return Mono.just(finalResponse);
            });
    }
}
