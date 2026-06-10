package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IFunzionalitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
public class FunzionalitaServiceImpl implements IFunzionalitaService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(FunzionalitaServiceImpl.class);

    public FunzionalitaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }



    @Override
    public Mono<GenericResponseDTO<List<FunzionalitaDTO>>> getFunzionalitàByRuolo(List<String> ruoli) {
        log.info("Richiesta lista di tutte le funzionalità sulla base del ruolo passato");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post(
                "/funzionalita/by-ruolo",
                webServiceType,
                ruoli, // body della POST (lista di stringhe)
                headers,
                new ParameterizedTypeReference<List<FunzionalitaDTO>>() {}
            )
            .doOnNext(response -> log.info("Numero funzionalità ricevute: {}", response.size()))
            .map(funzionalitaList -> {
                GenericResponseDTO<List<FunzionalitaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(funzionalitaList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
            });
    }

}
