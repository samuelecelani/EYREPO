package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IEventoRischioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class EventoRischioServiceImpl implements IEventoRischioService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(EventoRischioServiceImpl.class);

    public EventoRischioServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<EventoRischioDTO>> saveOrUpdate(EventoRischioDTO request) {
        log.info("Richiesta salvataggio/modifica EventoRischio");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/evento-rischio/save", webServiceType, request, headers, EventoRischioDTO.class)
            .doOnNext(response -> log.info("EventoRischio salvato/modificato: {}", response))
            .map(eventoRischioDTO -> {
                GenericResponseDTO<EventoRischioDTO> finalResponse = new GenericResponseDTO<>();

                if (eventoRischioDTO == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica EventoRischio");
                }

                finalResponse.setData(eventoRischioDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica EventoRischio: {}", e.getMessage(), e);

                GenericResponseDTO<EventoRischioDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<EventoRischioDTO>>> getAllByAttivitaSensibile(Long idAttivitaSensibile) {
        log.info("Richiesta recupero EventoRischio per AttivitaSensibile con id={}", idAttivitaSensibile);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/evento-rischio/attivita-sensibile/" + idAttivitaSensibile, webServiceType, headers,
                new ParameterizedTypeReference<List<EventoRischioDTO>>() {})
            .doOnNext(response ->
                log.info("EventoRischio recuperati: {} elementi", response != null ? response.size() : 0))
            .map(eventoRischioDTOList -> {
                GenericResponseDTO<List<EventoRischioDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(eventoRischioDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero EventoRischio per AttivitaSensibile id={}: {}", idAttivitaSensibile, e.getMessage(), e);

                GenericResponseDTO<List<EventoRischioDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione EventoRischio con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/evento-rischio/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("EventoRischio con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione EventoRischio con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

    @Override
    public Mono<Void> deleteByAttivitaSensibile(Long idAttivitaSensibile) {
        log.info("Richiesta cancellazione EventiRischio per AttivitaSensibile con id={}", idAttivitaSensibile);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/evento-rischio/attivita-sensibile/" + idAttivitaSensibile, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("EventiRischio per AttivitaSensibile con id={} cancellati con successo", idAttivitaSensibile))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione EventiRischio per AttivitaSensibile con id={}: {}", idAttivitaSensibile, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
