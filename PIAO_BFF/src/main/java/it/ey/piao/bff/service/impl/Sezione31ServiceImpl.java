package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ISezione31Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class Sezione31ServiceImpl implements ISezione31Service
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(Sezione31ServiceImpl.class);

    public Sezione31ServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione31DTO request)
    {
        log.info("Richiesta salvataggio/modifica Sezione31");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione31/save", webServiceType, request, headers, Void.class)
            .doOnNext(response -> log.info("Sezione31 Salvata/Modificata"))
            .map(result -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione31 {}", e.getMessage(), e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id)
    {
        log.info("Richiesta validazione stato Sezione31 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione31/validazione/" + id, webServiceType, new Sezione31DTO(), headers, Void.class)
            .doOnNext(response -> log.info("Modifica Stato Sezione31: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione31 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione31DTO>> getOrCreate(PiaoDTO request)
    {
        log.info("Ricerca Sezione31 per idPiao: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/sezione31/piao";
        return webClientService.post(url, webServiceType,request, headers, Sezione31DTO.class)
            .doOnNext(response -> log.info("Sezione31 trovata per idPiao {}: {}", request, response))
            .map(sezione31 -> {
                GenericResponseDTO<Sezione31DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sezione31);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca di Sezione31 per idPiao {}: {}", request, e.getMessage());
                GenericResponseDTO<Sezione31DTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione31DTO>> findByPiao(Long idPiao)
    {
        log.info("Ricerca Sezione31 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione31/"+ idPiao ,webServiceType,headers, Sezione31DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione31-> {
                GenericResponseDTO<Sezione31DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione31 == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione31");
                }
                finalResponse.setData(sezione31);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione31 {}", e);
                GenericResponseDTO<Sezione31DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
