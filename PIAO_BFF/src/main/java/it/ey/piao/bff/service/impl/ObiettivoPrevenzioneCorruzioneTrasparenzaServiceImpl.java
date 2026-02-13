package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IObiettivoPrevenzioneCorruzioneTrasparenzaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl implements IObiettivoPrevenzioneCorruzioneTrasparenzaService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl.class);

    public ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>> saveOrUpdate(ObiettivoPrevenzioneCorruzioneTrasparenzaDTO request) {
        log.info("Richiesta salvataggio/modifica ObiettivoPrevenzioneCorruzioneTrasparenza");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/obiettivo-prevenzione-corruzione-trasparenza/save", webServiceType, request, headers, ObiettivoPrevenzioneCorruzioneTrasparenzaDTO.class)
            .doOnNext(response -> log.info("ObiettivoPrevenzioneCorruzioneTrasparenza Salvato/Modificato: {}", response))
            .map(obiettivo -> {
                GenericResponseDTO<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> finalResponse = new GenericResponseDTO<>();
                if (obiettivo == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(obiettivo);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica ObiettivoPrevenzioneCorruzioneTrasparenza: {}", e.getMessage(), e);
                GenericResponseDTO<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
            }

    @Override
    public Mono<GenericResponseDTO<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>>> getAllBySezione23(Long idSezione23) {
        log.info("Richiesta recupero ObiettivoPrevenzioneCorruzioneTrasparenza per Sezione23 con id={}", idSezione23);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/obiettivo-prevenzione-corruzione-trasparenza/sezione23/" + idSezione23, webServiceType, headers,
                new ParameterizedTypeReference<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>>() {} // stesso schema dell'altro metodo
            )
            .doOnNext(response -> log.info("ObiettivoPrevenzioneCorruzioneTrasparenza recuperati: {} elementi", response != null ? response.size() : 0))
            .map(obiettivi -> {
                GenericResponseDTO<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obiettivi);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero ObiettivoPrevenzioneCorruzioneTrasparenza per Sezione23 id={}: {}", idSezione23, e.getMessage(), e);
                GenericResponseDTO<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione ObiettivoPrevenzioneCorruzioneTrasparenza con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.delete("/obiettivo-prevenzione-corruzione-trasparenza/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("ObiettivoPrevenzioneCorruzioneTrasparenza con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione ObiettivoPrevenzioneCorruzioneTrasparenza con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }


}


