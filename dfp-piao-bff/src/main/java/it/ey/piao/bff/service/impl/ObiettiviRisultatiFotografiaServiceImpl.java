package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IObiettiviRisultatiFotografiaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ObiettiviRisultatiFotografiaServiceImpl implements IObiettiviRisultatiFotografiaService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(ObiettiviRisultatiFotografiaServiceImpl.class);

    public ObiettiviRisultatiFotografiaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<ObiettiviRisultatiFotografiaDTO>> saveOrUpdate(ObiettiviRisultatiFotografiaDTO request) {
        log.info("Richiesta salvataggio/modifica ObiettiviRisultatiFotografia");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .post("/obiettivi-risultati-fotografia/save", webServiceType, request, headers, ObiettiviRisultatiFotografiaDTO.class)
            .doOnNext(response -> log.info("ObiettiviRisultatiFotografia salvato/modificato: {}", response))
            .map(obiettivo -> {
                GenericResponseDTO<ObiettiviRisultatiFotografiaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());

                if (obiettivo == null) {
                    finalResponse.getStatus().setSuccess(Boolean.FALSE);
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica ObiettiviRisultatiFotografia");
                    finalResponse.setData(null);
                    return finalResponse;
                }

                finalResponse.setData(obiettivo);
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica ObiettiviRisultatiFotografia: {}", e.getMessage(), e);

                GenericResponseDTO<ObiettiviRisultatiFotografiaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>>> getObiettiviRisultatiBySezione332(Long idSezione332) {
        log.info("Richiesta recupero ObiettiviRisultati per Sezione332 con id={}", idSezione332);
        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // chiamata get al BE per ottenre il risultato
        return webClientService.get("/obiettivi-risultati-fotografia/obiettivi-risultati/" + idSezione332, webServiceType, headers,
                new ParameterizedTypeReference<List<ObiettiviRisultatiFotografiaDTO>>() {
                })

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                    log.info("ObiettiviRisultati recuperati: {} elementi", response != null ? response.size() : 0)

                // mappiamo la lista obiettivi ,settando status e i dati ricevuti
            ).map(obiettivi -> {
                GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obiettivi);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            // Gestione errori della chiamata get al BE
            .onErrorResume(e -> {
                log.error("Errore recupero ObiettiviRisultati per Sezione332 id={}: {}", idSezione332, e.getMessage(), e);

                GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                // ritorniamo il mono ( contenitore reattivo che può contenere zero o 1 elementi tra cui anche un errore)
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>>> getFotografieFormazioneBySezione332(Long idSezione332) {
        log.info("Richiesta recupero FotografieFormazione per Sezione332 con id={}", idSezione332);
        HttpHeaders headers = new HttpHeaders();
        // Risposta in frormato JSON
        headers.set("Accept", "application/json");

        // chiamata get al BE per ottenre il risultato
        return webClientService.get("/obiettivi-risultati-fotografia/fotografia-formazione/" + idSezione332, webServiceType, headers,
                new ParameterizedTypeReference<List<ObiettiviRisultatiFotografiaDTO>>() {
                })

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                    log.info("FotografieFormazione recuperati: {} elementi", response != null ? response.size() : 0)

                // mappiamo la lista obiettivi ,settando status e i dati ricevuti
            ).map(obiettivi -> {
                GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obiettivi);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            // Gestione errori della chiamata get al BE
            .onErrorResume(e -> {
                log.error("Errore recupero FotografieFormazione per Sezione332 id={}: {}", idSezione332, e.getMessage(), e);

                GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                // ritorniamo il mono ( contenitore reattivo che può contenere zero o 1 elementi tra cui anche un errore)
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione) {
        // log della richiesta
        log.info("Richiesta cancellazione ObiettiviRisultatiFotografia con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        StringBuilder url = new StringBuilder("/obiettivi-risultati-fotografia/" + id + "?");
        if (campiModificati != null && !campiModificati.isBlank()) url.append("campiModificati=").append(campiModificati).append("&");
        if (idPiao != null) url.append("idPiao=").append(idPiao).append("&");
        if (testoSezione != null && !testoSezione.isBlank()) url.append("testoSezione=").append(testoSezione);
        //chiamata delete
        return webClientService.delete(url.toString(), webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("ObiettiviRisultatiFotografiaDTO con id={} cancellato con successo", id))
            .then()
            // in caso di errore logghiamo l'errore e ritorniamo un mono
            .onErrorResume(e -> {
                log.error("Errore cancellazione ObiettiviRisultatiFotografiaDTO con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

}
