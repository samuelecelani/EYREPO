package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ISezione331Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class Sezione331ServiceImpl implements ISezione331Service {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(Sezione331ServiceImpl.class);

    public Sezione331ServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<Sezione331DTO>> getOrCreate(PiaoDTO request) {
        log.info("Ricerca Sezione331 per idPiao: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/sezione331/piao";
        return webClientService.post(url, webServiceType, request, headers, Sezione331DTO.class)
            .doOnNext(response -> log.info("Sezione331 trovata per idPiao {}: {}", request, response))
            .map(sezione331 -> {
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sezione331);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca di Sezione331 per idPiao {}: {}", request, e.getMessage());
                GenericResponseDTO<Sezione331DTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<Sezione331DTO>> saveOrUpdate(Sezione331DTO request) {
        log.info("Richiesta salvataggio/aggiornamento Sezione331");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione331/save", webServiceType, request, headers, Sezione331DTO.class)
            .doOnNext(response -> log.info("Sezione331 Salvata/Modificata: {}", response))
            .map(sezione331 -> {
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione331 == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(sezione331);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione331 {}", e);
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id) {
        log.info("Richiesta validazione stato Sezione331 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione331/validazione/" + id, webServiceType, new Sezione331DTO(), headers, Void.class)
            .doOnNext(response -> log.info("Modifica Stato Sezione331: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione331 {}", e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione331DTO>> findByPiao(Long idPiao) {
        log.info("Ricerca Sezione331 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione331/" + idPiao, webServiceType, headers, Sezione331DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione331 -> {
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione331 == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione");
                }
                finalResponse.setData(sezione331);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione331 {}", e);
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
