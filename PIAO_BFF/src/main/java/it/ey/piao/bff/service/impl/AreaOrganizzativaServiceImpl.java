package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAreaOrganizzativaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Service
public class AreaOrganizzativaServiceImpl implements IAreaOrganizzativaService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(AreaOrganizzativaServiceImpl.class);

    public AreaOrganizzativaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<List<AreaOrganizzativaDTO>>> findByidSezione1(Long idSezione1) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/aree-organizzative/sezione/"+ idSezione1, webServiceType,headers,new ParameterizedTypeReference<List<AreaOrganizzativaDTO>>() {})
            .doOnNext(response -> log.info(" Numero di Aree organizzative ricevute: {}", response.size()))
            .map(statoList -> {
                GenericResponseDTO<List<AreaOrganizzativaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(statoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<AreaOrganizzativaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
    @Override
    public Mono<GenericResponseDTO<List<AreaOrganizzativaDTO>>> findByPiaoId(Long piaoId) {
        log.info("Recupero di tutte le aree organizzative per PIAO id={}", piaoId);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/aree-organizzative/piao/" + piaoId, webServiceType, headers, new ParameterizedTypeReference<List<AreaOrganizzativaDTO>>() {})
            .doOnNext(response -> log.info("Numero aree organizzative ricevute per PIAO {}: {}", piaoId, response.size()))
            .map(areeOrganizzative -> {
                GenericResponseDTO<List<AreaOrganizzativaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(areeOrganizzative);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle aree organizzative per PIAO {}: {}", piaoId, e.getMessage(), e);
                GenericResponseDTO<List<AreaOrganizzativaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AreaOrganizzativaDTO>> save(AreaOrganizzativaDTO request) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/aree-organizzative/save",webServiceType,request,headers, AreaOrganizzativaDTO.class)
            .doOnNext(response -> log.info("Sezione1 Salvata/Modficata: {}", response))
            .map(sezione1-> {
                GenericResponseDTO<AreaOrganizzativaDTO> finalResponse = new GenericResponseDTO<>();
                if (sezione1 == null){
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(sezione1);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione1 {}", e);
                GenericResponseDTO<AreaOrganizzativaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
