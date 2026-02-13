package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.DimensioneIndicatoreDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IDimensioneIndicatoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DimensioneIndicatoreServiceImpl implements IDimensioneIndicatoreService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(DimensioneIndicatoreServiceImpl.class);

    public DimensioneIndicatoreServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<DimensioneIndicatoreDTO>>> getDimensioneIndicatore(String codTipologiaFK) {
        log.info("Richiesta lista di tutte le dimensioni indicatore per codTipologiaFK: {}", codTipologiaFK);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/dimensione-indicatore?codTipologiaFK=" + codTipologiaFK;
        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<DimensioneIndicatoreDTO>>() {})
            .doOnNext(response -> log.info("Numero dimensioni indicatore ricevute: {}", response.size()))
            .map(dimensioneList -> {
                GenericResponseDTO<List<DimensioneIndicatoreDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(dimensioneList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle dimensioni indicatore: {}", e.getMessage());
                GenericResponseDTO<List<DimensioneIndicatoreDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
