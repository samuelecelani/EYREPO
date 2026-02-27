package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IObbligoLeggeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ObbligoLeggeServiceImpl implements IObbligoLeggeService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType ;
    private static final Logger log = LoggerFactory.getLogger(ObbligoLeggeServiceImpl.class);

    public ObbligoLeggeServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<ObbligoLeggeDTO>> saveOrUpdate(ObbligoLeggeDTO request) {
        log.info("Richiesta salvataggio/modifica ObbligoLegge");

        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // chiamata post verso l'endpoint del BE
        return webClientService.post("/obbligo-legge/save", webServiceType, request, headers, ObbligoLeggeDTO.class)

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                log.info("ObbligoLegge salvato/modificato: {}", response))

            // mappiamo la risposta
            .map(obbligo -> {GenericResponseDTO<ObbligoLeggeDTO> finalResponse = new GenericResponseDTO<>();

                // se ci sono errori
                if (obbligo == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError()
                        .setMessageError("Errore nel salvataggio/modifica ObbligoLegge");
                }
                // inseriamo i dati
                finalResponse.setData(obbligo);

                // crea e valorizza lo status
                finalResponse.setStatus(new Status());

                // se tutto regolare indichiamo che l'operazione è andata a buon fine
                finalResponse.getStatus().setSuccess(Boolean.TRUE);


                return finalResponse;
            })
            // gestiamo gli errori HTTP con log dell'errore
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica ObbligoLegge: {}",
                    e.getMessage(), e);
                // costruiamo la risposta dell'errore  ,status fallito
                GenericResponseDTO<ObbligoLeggeDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                // restituiamo un Mono con la risposta dell'errore
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<ObbligoLeggeDTO>>> getAllBySezione23(Long idSezione23) {
        log.info("Richiesta recupero ObbligoLegge per Sezione23 con id={}", idSezione23);
        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // chiamata get al BE per ottenre il risultato
        return webClientService.get("/obbligo-legge/sezione23/" + idSezione23, webServiceType, headers,
                new ParameterizedTypeReference<List<ObbligoLeggeDTO>>() {
                })

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                    log.info("ObbligoLegge recuperati: {} elementi", response != null ? response.size() : 0)

                // mappiamo la lista obiettivi ,settando status e i dati ricevuti
            ).map(obbligo -> {
                GenericResponseDTO<List<ObbligoLeggeDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obbligo);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            // Gestione errori della chiamata get al BE
            .onErrorResume(e -> {
            log.error("Errore recupero ObbligoLegge per Sezione23 id={}: {}", idSezione23, e.getMessage(), e);

            GenericResponseDTO<List<ObbligoLeggeDTO>> errorResponse = new GenericResponseDTO<>();
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
        log.info("Richiesta cancellazione Obbligo Legge con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        //chiamata delete
        return webClientService.delete("/obbligo-legge/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("ObbligoLegge con id={} cancellato con successo", id))
            .then()
            // in caso di errore logghiamo l'errore e ritorniamo un mono
            .onErrorResume(e -> {
                log.error("Errore cancellazione ObbligoLegge con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
