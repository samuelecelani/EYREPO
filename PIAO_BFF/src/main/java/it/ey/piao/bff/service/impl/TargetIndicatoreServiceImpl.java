package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.dto.TargetIndicatoreDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ITargetIndicatoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TargetIndicatoreServiceImpl implements ITargetIndicatoreService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(TargetIndicatoreServiceImpl.class);

    public TargetIndicatoreServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<TargetIndicatoreDTO>>> getTargetIndicatore() {
        log.info("Richiesta lista di tutti i target indicatore");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/target-indicatore", webServiceType, headers, new ParameterizedTypeReference<List<TargetIndicatoreDTO>>() {})
            .doOnNext(response -> log.info("Numero target indicatore ricevuti: {}", response.size()))
            .map(targetList -> {
                GenericResponseDTO<List<TargetIndicatoreDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(targetList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero dei target indicatore: {}", e.getMessage());
                GenericResponseDTO<List<TargetIndicatoreDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
