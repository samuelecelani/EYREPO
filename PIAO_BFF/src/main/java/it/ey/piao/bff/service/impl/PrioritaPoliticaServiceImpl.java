package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IPrioritaPoliticaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
public class PrioritaPoliticaServiceImpl implements IPrioritaPoliticaService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(PrioritaPoliticaServiceImpl.class);

    public PrioritaPoliticaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<List<PrioritaPoliticaDTO>>> findByidSezione1(Long idSezione1) {
        log.info("Recupero la lista delle priorità politiche");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/priorita-politiche/sezione/"+ idSezione1, webServiceType,headers,new ParameterizedTypeReference<List<PrioritaPoliticaDTO>>() {})
            .doOnNext(response -> log.info(" Numero di Aree organizzative ricevute: {}", response.size()))
            .map(statoList -> {
                GenericResponseDTO<List<PrioritaPoliticaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(statoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<PrioritaPoliticaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PrioritaPoliticaDTO>>> findByPiaoId(Long piaoId) {
        log.info("Recupero di tutte le priorità politiche per PIAO id={}", piaoId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/priorita-politiche/piao/" + piaoId, webServiceType, headers, new ParameterizedTypeReference<List<PrioritaPoliticaDTO>>() {})
            .doOnNext(response -> log.info("Numero priorità politiche ricevute per PIAO {}: {}", piaoId, response.size()))
            .map(prioritaPolitiche -> {
                GenericResponseDTO<List<PrioritaPoliticaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(prioritaPolitiche);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle priorità politiche per PIAO {}: {}", piaoId, e.getMessage(), e);
                GenericResponseDTO<List<PrioritaPoliticaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<PrioritaPoliticaDTO>> save(PrioritaPoliticaDTO request) {
        log.info("Salvataggio priorità politica");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/priorita-politiche/save",webServiceType,request,headers, PrioritaPoliticaDTO.class)
            .doOnNext(response -> log.info("priorita politica Salvata/Modficata: {}", response))
            .map(prioritaPolitica-> {
                GenericResponseDTO<PrioritaPoliticaDTO> finalResponse = new GenericResponseDTO<>();
                if (prioritaPolitica == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(prioritaPolitica);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica priorita politica {}", e);
                GenericResponseDTO<PrioritaPoliticaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
