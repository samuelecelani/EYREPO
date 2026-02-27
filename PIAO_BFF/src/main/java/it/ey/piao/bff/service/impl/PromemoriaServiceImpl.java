package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IPromemoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class PromemoriaServiceImpl implements IPromemoriaService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(PromemoriaServiceImpl.class);

    public PromemoriaServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<PromemoriaDTO>>> getAll()
    {
        log.info("Richiesta recupero Promemoria");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/promemoria", webServiceType, headers,
                new ParameterizedTypeReference<List<PromemoriaDTO>>() {})
            .doOnNext(response ->
                log.info("Promemoria recuperati: {} elementi", response != null ? response.size() : 0))
            .map(promemoriaDTOList -> {
                GenericResponseDTO<List<PromemoriaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(promemoriaDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero Promemoria", e.getMessage(), e);

                GenericResponseDTO<List<PromemoriaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }
}
