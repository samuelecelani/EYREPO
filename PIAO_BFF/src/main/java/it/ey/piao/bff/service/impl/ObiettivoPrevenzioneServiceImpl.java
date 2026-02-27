package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IObiettivoPrevenzioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ObiettivoPrevenzioneServiceImpl implements IObiettivoPrevenzioneService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(ObiettivoPrevenzioneServiceImpl.class);

    public ObiettivoPrevenzioneServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<ObiettivoPrevenzioneDTO>> saveOrUpdate(ObiettivoPrevenzioneDTO request) {
        log.info("Richiesta salvataggio/modifica ObiettivoPrevenzione");

        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // // chiamata post verso l'endpoint del BE
        return webClientService.post("/obiettivo-prevenzione/save", webServiceType, request, headers, ObiettivoPrevenzioneDTO.class)

           // logghiamo la risposta ricevuta
            .doOnNext(response ->
                log.info("ObiettivoPrevenzione salvato/modificato: {}", response))

            // mappiamo la risposta
            .map(obiettivo -> {GenericResponseDTO<ObiettivoPrevenzioneDTO> finalResponse = new GenericResponseDTO<>();

                // se ci sono errori
                if (obiettivo == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError()
                        .setMessageError("Errore nel salvataggio/modifica ObiettivoPrevenzione");
                }
                  // inseriamo i dati
                finalResponse.setData(obiettivo);

                // crea e valorizza lo status
                finalResponse.setStatus(new Status());

                // se tutto regolare indichiamo che l'operazione è andata a buon fine
                finalResponse.getStatus().setSuccess(Boolean.TRUE);


                return finalResponse;
            })
            // gestiamo gli errori HTTP con log dell'errore
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica ObiettivoPrevenzione: {}",
                    e.getMessage(), e);
                // costruiamo la risposta dell'errore  ,status fallito
                GenericResponseDTO<ObiettivoPrevenzioneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                // restituiamo un Mono con la risposta dell'errore
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<List<ObiettivoPrevenzioneDTO>>> getAllBySezione23(Long idSezione23) {
        log.info("Richiesta recupero ObiettiviPrevenzione per Sezione23 con id={}", idSezione23);
        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // chiamata get al BE per ottenre il risultato
        return webClientService.get("/obiettivo-prevenzione/sezione23/" + idSezione23, webServiceType, headers,
                new ParameterizedTypeReference<List<ObiettivoPrevenzioneDTO>>() {
                })

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                    log.info("ObiettiviPrevenzione recuperati: {} elementi", response != null ? response.size() : 0)

                // mappiamo la lista obiettivi ,settando status e i dati ricevuti
            ).map(obiettivi -> {
                GenericResponseDTO<List<ObiettivoPrevenzioneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obiettivi);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })

            // Gestione errori della chiamata get al BE
            .onErrorResume(e -> {
                log.error("Errore recupero ObiettiviPrevenzione per Sezione23 id={}: {}", idSezione23, e.getMessage(), e);

                GenericResponseDTO<List<ObiettivoPrevenzioneDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                // ritorniamo il mono ( contenitore reattivo che può contenere zero o 1 elementi tra cui anche un errore)
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        // log della richiesta
        log.info("Richiesta cancellazione ObiettivoPrevenzione con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        //chiamata delete
        return webClientService.delete("/obiettivo-prevenzione/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("ObiettivoPrevenzione con id={} cancellato con successo", id))
            .then()
            // in caso di errore logghiamo l'errore e ritorniamo un mono
            .onErrorResume(e -> {
                log.error("Errore cancellazione ObiettivoPrevenzione con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

}
