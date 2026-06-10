package it.ey.piao.bff.service.impl;

import it.ey.dto.AutoritaApprovatoreDTO;
import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAutoritaApprovatoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AutoritaApprovatoreServiceImpl implements IAutoritaApprovatoreService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(AutoritaApprovatoreServiceImpl.class);

    public AutoritaApprovatoreServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<AutoritaApprovatoreDTO>>> getAll()
    {
        log.info("Richiesta recupero AutoritaApprovatore");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/autorita-approvatore", webServiceType, headers,
                new ParameterizedTypeReference<List<AutoritaApprovatoreDTO>>() {})
            .doOnNext(response ->
                log.info("AutoritaApprovatore recuperati: {} elementi",
                    response != null ? response.size() : 0))
            .map(dtoList -> {
                GenericResponseDTO<List<AutoritaApprovatoreDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(dtoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero AutoritaApprovatore", e.getMessage(), e);

                GenericResponseDTO<List<AutoritaApprovatoreDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }
}
