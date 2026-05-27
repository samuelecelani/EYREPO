package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStatoPIAOService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
public class StatoPIAOServiceImpl implements IStatoPIAOService {


    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(StatoPIAOServiceImpl.class);

    public StatoPIAOServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<List<StatoPIAODTO>>> getStatoPIAO() {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/stato/piao", webServiceType,headers,new ParameterizedTypeReference<List<StatoPIAODTO>>() {})
            .doOnNext(response -> log.info(" Numero stati ricevuti: {}", response.size()))
            .map(statoList -> {
                GenericResponseDTO<List<StatoPIAODTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(statoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<StatoPIAODTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
