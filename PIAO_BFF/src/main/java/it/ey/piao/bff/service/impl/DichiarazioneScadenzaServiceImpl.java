package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IDichiarazioneScadenzaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DichiarazioneScadenzaServiceImpl implements IDichiarazioneScadenzaService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(DichiarazioneScadenzaServiceImpl.class);

    public DichiarazioneScadenzaServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> saveOrUpdate(DichiarazioneScadenzaDTO request)
    {
        log.info("Richiesta salvataggio/modifica DichiarazioneScadenza");

        HttpHeaders headers = new HttpHeaders();
        // Risposta in formato JSON
        headers.set("Accept", "application/json");

        // chiamata post verso l'endpoint del BE
        return webClientService.post("/dichiarazione-scadenza", webServiceType, request, headers, DichiarazioneScadenzaDTO.class)

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                log.info("DichiarazioneScadenza salvato/modificato: {}", response))

            // mappiamo la risposta
            .map(dichiarazione -> {GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();

                // se ci sono errori
                if (dichiarazione == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError()
                        .setMessageError("Errore nel salvataggio/modifica DichiarazioneScadenza");
                }
                // inseriamo i dati
                finalResponse.setData(dichiarazione);

                // crea e valorizza lo status
                finalResponse.setStatus(new Status());

                // se tutto regolare indichiamo che l'operazione è andata a buon fine
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            // gestiamo gli errori HTTP con log dell'errore
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica DichiarazioneScadenza: {}",
                    e.getMessage(), e);
                // costruiamo la risposta dell'errore  ,status fallito
                GenericResponseDTO<DichiarazioneScadenzaDTO> errorResponse = new GenericResponseDTO<>();
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

        log.info("Richiesta cancellazione DichiarazioneScadenza con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        //chiamata delete
        return webClientService.delete("/dichiarazione-scadenza/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("DichiarazioneScadenza con id={} cancellata con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione DichiarazioneScadenza con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

    @Override
    public Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> getExistingDichiarazioneScadenza(String codPAFK) {
        log.info("Richiesta recupero DichiarazioneScadenzaDTO per codPAFK", codPAFK);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/dichiarazione-scadenza/" + codPAFK, webServiceType, headers, DichiarazioneScadenzaDTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(d -> {
                GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();
                if (d == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero DichiarazioneScadenzaDTO per codPAFK: " + codPAFK);
                }
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica DichiarazioneScadenzaDTO {}", e);
                GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
