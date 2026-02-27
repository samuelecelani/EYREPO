package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IMisuraPrevenzioneEventoRischioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MisuraPrevenzioneEventoRischioServiceImpl implements IMisuraPrevenzioneEventoRischioService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(ObiettivoPrevenzioneCorruzioneTrasparenzaServiceImpl.class);

    public MisuraPrevenzioneEventoRischioServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<MisuraPrevenzioneEventoRischioDTO>> saveOrUpdate(MisuraPrevenzioneEventoRischioDTO request) {
        log.info("Richiesta salvataggio/modifica MisuraPrevenzioneEventoRischio");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/misura-prevenzione-evento-rischio/save", webServiceType, request, headers, MisuraPrevenzioneEventoRischioDTO.class)
            .doOnNext(response -> log.info("MisuraPrevenzioneEventoRischio Salvato/Modificato: {}", response))
            .map(misura -> {
                GenericResponseDTO<MisuraPrevenzioneEventoRischioDTO> finalResponse = new GenericResponseDTO<>();
                if (misura == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(misura);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica MisuraPrevenzioneEventoRischio: {}", e.getMessage(), e);
                GenericResponseDTO<MisuraPrevenzioneEventoRischioDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<MisuraPrevenzioneEventoRischioDTO>>> getMisuraPrevenzioneByEventoRischio(Long idEventoRischio) {
        log.info("Richiesta recupero MisuraPrevenzioneEventoRischio per EventoRischio id={}", idEventoRischio);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/misura-prevenzione-evento-rischio/eventoRischio/" + idEventoRischio,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<MisuraPrevenzioneEventoRischioDTO>>() {}
            )
            .doOnNext(response -> log.info("MisurePrevenzioneEventoRischio recuperate: {} elementi", response != null ? response.size() : 0))
            .map(misure -> {
                GenericResponseDTO<List<MisuraPrevenzioneEventoRischioDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(misure);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero MisuraPrevenzioneEventoRischio per EventoRischio id={}: {}", idEventoRischio, e.getMessage(), e);
                GenericResponseDTO<List<MisuraPrevenzioneEventoRischioDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione MisuraPrevenzioneEventoRischio con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/misura-prevenzione-evento-rischio/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("MisuraPrevenzioneEventoRischio con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione MisuraPrevenzioneEventoRischio con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

    @Override
    public Mono<Void> deleteByEventoRischio(Long idEventoRischio) {
        log.info("Richiesta cancellazione MisuraPrevenzioneEventoRischio per EventoRischio id={}", idEventoRischio);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/misura-prevenzione-evento-rischio/eventoRischio/" + idEventoRischio, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("MisuraPrevenzioneEventoRischio per EventoRischio id={} cancellate con successo", idEventoRischio))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione MisuraPrevenzioneEventoRischio per EventoRischio id={}: {}", idEventoRischio, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
