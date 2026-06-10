package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.mapper.AmministrazioneMapper;
import it.ey.piao.bff.service.IAmministrazioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AmministrazioneServiceImpl implements IAmministrazioneService {

    private static final Logger log = LoggerFactory.getLogger(AmministrazioneServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    @Autowired
    public AmministrazioneServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<AmministrazioneInternalDTO>>> search(
            String codiceIpa,
            String tipologia,
            String denominazione) {

        log.info("Ricerca amministrazioni su BE - codiceIpa={}, tipologia={}, denominazione={}",
                codiceIpa, tipologia, denominazione);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/anagrafica/search");
        List<String> params = new ArrayList<>();

        if (codiceIpa != null && !codiceIpa.isBlank()) {
            params.add("codiceIpa=" + codiceIpa);
        }
        if (tipologia != null && !tipologia.isBlank()) {
            params.add("tipologia=" + tipologia);
        }
        if (denominazione != null && !denominazione.isBlank()) {
            params.add("denominazione=" + denominazione);
        }

        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        return webClientService.get(
                        url.toString(),
                        webServiceType,
                        headers,
                        new ParameterizedTypeReference<List<AnagraficaDTO>>() {}
                )
                .doOnNext(response -> log.info("Amministrazioni recuperate dal BE: {} elementi",
                        response != null ? response.size() : 0))
                .map(anagraficaList -> {
                    GenericResponseDTO<List<AmministrazioneInternalDTO>> finalResponse = new GenericResponseDTO<>();
                    finalResponse.setStatus(new Status());
                    finalResponse.getStatus().setSuccess(Boolean.TRUE);

                    List<AmministrazioneInternalDTO> internalList = AmministrazioneMapper.toInternalList(anagraficaList);
                    finalResponse.setData(internalList);

                    return finalResponse;
                })
                .onErrorResume(e -> {
                    log.error("Errore nel recupero amministrazioni dal BE: {}", e.getMessage(), e);
                    GenericResponseDTO<List<AmministrazioneInternalDTO>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError(e.getMessage());
                    errorResponse.setData(Collections.emptyList());
                    return Mono.just(errorResponse);
                });
    }

    @Override
    public Mono<GenericResponseDTO<List<String>>> getTipologie() {

        log.info("Recupero tipologie amministrazioni dal BE");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                        "/anagrafica/tipologie",
                        webServiceType,
                        headers,
                        new ParameterizedTypeReference<List<String>>() {}
                )
                .doOnNext(response -> log.info("Tipologie recuperate dal BE: {} elementi",
                        response != null ? response.size() : 0))
                .map(tipologie -> {
                    GenericResponseDTO<List<String>> finalResponse = new GenericResponseDTO<>();
                    finalResponse.setStatus(new Status());
                    finalResponse.getStatus().setSuccess(Boolean.TRUE);
                    finalResponse.setData(tipologie);
                    return finalResponse;
                })
                .onErrorResume(e -> {
                    log.error("Errore nel recupero tipologie dal BE: {}", e.getMessage(), e);
                    GenericResponseDTO<List<String>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError(e.getMessage());
                    errorResponse.setData(Collections.emptyList());
                    return Mono.just(errorResponse);
                });
    }
}
