package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ISezione1Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class Sezione1ServiceImpl implements ISezione1Service {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(Sezione1ServiceImpl.class);

    public Sezione1ServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione1DTO request) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione1/save",webServiceType,request,headers,Void.class)
            .doOnNext(response -> log.info("Sezione1 Salvata/Modficata: {}", response))
            .map(sezione1-> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione1 {}", e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
    @Override
    public Mono<GenericResponseDTO<Sezione1DTO>> findByPiao(Long idPiao) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione1/"+ idPiao ,webServiceType,headers,Sezione1DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione1-> {
                GenericResponseDTO<Sezione1DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione1 == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione");
                }
                finalResponse.setData(sezione1);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione1 {}", e);
                GenericResponseDTO<Sezione1DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id) {
        log.info("Richiesta validazione stato Sezione1 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione1/validazione/" + id, webServiceType, new Sezione1DTO(), headers, Void.class)
            .doOnNext(response -> log.info("Modifica Stato Sezione1: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione1 {}", e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
        //creare file risorsa statica json (codice_notifica,testo,tipo,ruolo, modulo_id)
        //creare metodo centralizzato che interroghi il file sopra e dato il codice_notifica (richiesta_Validazione_sez1) ritorni la mappatura
        //per ogni ruolo chiamare tornato dalla mappatura creare un NotificationDTO aggregando i dati dinamici (sender, ecc)
        //Add NotificationDTO nella lista List<NotificationDTO>
        //sendNotificationBatch(List<NotificationDTO> notifications)
    }

}
