package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IMotivazioneDichiarazioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MotivazioneDichiarazioneServiceImpl implements IMotivazioneDichiarazioneService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(MotivazioneDichiarazioneServiceImpl.class);

    public MotivazioneDichiarazioneServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<MotivazioneDichiarazioneDTO>> saveOrUpdate(MotivazioneDichiarazioneDTO request)
    {
        log.info("Richiesta salvataggio/modifica MotivazioneDichiarazione");

        HttpHeaders headers = new HttpHeaders();
        // Risposta in formato JSON
        headers.set("Accept", "application/json");

        // chiamata post verso l'endpoint del BE
        return webClientService.post("/motivazione-dichiarazione/save", webServiceType, request, headers, MotivazioneDichiarazioneDTO.class)

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                log.info("motivazioneDichiarazione salvato/modificato: {}", response))

            // mappiamo la risposta
            .map(motivazione -> {GenericResponseDTO<MotivazioneDichiarazioneDTO> finalResponse = new GenericResponseDTO<>();

                // se ci sono errori
                if (motivazione == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError()
                        .setMessageError("Errore nel salvataggio/modifica motivazioneDichiarazione");
                }
                // inseriamo i dati
                finalResponse.setData(motivazione);

                // crea e valorizza lo status
                finalResponse.setStatus(new Status());

                // se tutto regolare indichiamo che l'operazione è andata a buon fine
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            // gestiamo gli errori HTTP con log dell'errore
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica motivazioneDichiarazione: {}",
                    e.getMessage(), e);
                // costruiamo la risposta dell'errore  ,status fallito
                GenericResponseDTO<MotivazioneDichiarazioneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                // restituiamo un Mono con la risposta dell'errore
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id)
    {
        // log della richiesta

        log.info("Richiesta cancellazione MotivazioneDichiarazione con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        //chiamata delete
        return webClientService.delete("/motivazione-dichiarazione/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("MotivazioneDichiarazione con id={} cancellata con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione MotivazioneDichiarazione con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<MotivazioneDichiarazioneDTO>>> getAll() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/motivazione-dichiarazione", webServiceType, headers, new ParameterizedTypeReference<List<MotivazioneDichiarazioneDTO>>() {})
            .doOnNext(response -> log.info("Numero MotivazioneDichiarazione ricevuti : {}", response.size()))
            .map(md -> {
                GenericResponseDTO<List<MotivazioneDichiarazioneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(md);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero degli MotivazioneDichiarazione per PIAO {}", e.getMessage(), e);
                GenericResponseDTO<List<MotivazioneDichiarazioneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }
}
