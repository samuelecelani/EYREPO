package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IMisuraPrevenzioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MisuraPrevenzioneServiceImpl implements IMisuraPrevenzioneService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(MisuraPrevenzioneServiceImpl.class);

    public MisuraPrevenzioneServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<MisuraPrevenzioneDTO>> saveOrUpdate(MisuraPrevenzioneDTO request) {
        log.info("Richiesta salvataggio/modifica MisuraPrevenzione");

        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // // chiamata post verso l'endpoint del BE
        return webClientService.post("/misura-prevenzione/save", webServiceType, request, headers, MisuraPrevenzioneDTO.class)

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                log.info("misuraPrevenzione salvato/modificato: {}", response))

            // mappiamo la risposta
            .map(misura -> {GenericResponseDTO<MisuraPrevenzioneDTO> finalResponse = new GenericResponseDTO<>();

                // se ci sono errori
                if (misura == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError()
                        .setMessageError("Errore nel salvataggio/modifica misuraPrevenzione");
                }
                // inseriamo i dati
                finalResponse.setData(misura);

                // crea e valorizza lo status
                finalResponse.setStatus(new Status());

                // se tutto regolare indichiamo che l'operazione è andata a buon fine
                finalResponse.getStatus().setSuccess(Boolean.TRUE);


                return finalResponse;
            })
            // gestiamo gli errori HTTP con log dell'errore
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica misuraPrevenzione: {}",
                    e.getMessage(), e);
                // costruiamo la risposta dell'errore  ,status fallito
                GenericResponseDTO<MisuraPrevenzioneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                // restituiamo un Mono con la risposta dell'errore
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<MisuraPrevenzioneDTO>>> getMisuraPrevenzioneByObiettivoPrevenzione(Long idObiettivoPrevenzione) {
        log.info("Richiesta recupero MisuraPrevenzione per obiettiviPrevenzione con  id={}", idObiettivoPrevenzione);
        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // chiamata get al BE per ottenre il risultato
        return webClientService.get("/misura-prevenzione/obiettivo/" + idObiettivoPrevenzione, webServiceType, headers,
                new ParameterizedTypeReference<List<MisuraPrevenzioneDTO>>() {
                })

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                    log.info("MisuraPrevenzione recuperati: {} elementi", response != null ? response.size() : 0)

                // mappiamo la lista misure ,settando status e i dati ricevuti
            ).map(misure -> {
                GenericResponseDTO<List<MisuraPrevenzioneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(misure);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })

            // Gestione errori della chiamata get al BE
            .onErrorResume(e -> {
                log.error("Errore recupero MisurePrevenzione per biettiviPrevenzione id={}: {}", idObiettivoPrevenzione, e.getMessage(), e);

                GenericResponseDTO<List<MisuraPrevenzioneDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                // ritorniamo il mono ( contenitore reattivo che può contenere zero o 1 elementi tra cui anche un errore)
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<MisuraPrevenzioneDTO>>> getAllBySezione23(Long idSezione23) {
        log.info("Richiesta recupero MisuraPrevenzione per Sezione23 con id={}", idSezione23);

        if (idSezione23 == null) {
            GenericResponseDTO<List<MisuraPrevenzioneDTO>> errorResponse = new GenericResponseDTO<>();
            errorResponse.setStatus(new Status());
            errorResponse.getStatus().setSuccess(Boolean.FALSE);
            errorResponse.setError(new Error());
            errorResponse.getError().setMessageError("L'ID della Sezione23 non può essere nullo");
            return Mono.just(errorResponse);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/misura-prevenzione/sezione/" + idSezione23, webServiceType, headers,
                new ParameterizedTypeReference<List<MisuraPrevenzioneDTO>>() {})
            .doOnNext(response -> log.info("MisurePrevenzione recuperate: {} elementi", response != null ? response.size() : 0))
            .map(misure -> {
                GenericResponseDTO<List<MisuraPrevenzioneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(misure);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero MisurePrevenzione per Sezione23 id={}: {}", idSezione23, e.getMessage(), e);
                GenericResponseDTO<List<MisuraPrevenzioneDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }



    @Override
    public Mono<Void> deleteById(Long id) {
        // log della richiesta

        log.info("Richiesta cancellazione MisuraPrevenzione con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        //chiamata delete
        return webClientService.delete("/misura-prevenzione/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("MisuraPrevenzione con id={} cancellata con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione MisuraPrevenzione con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
