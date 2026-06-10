package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAvvisoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AvvisoServiceImpl implements IAvvisoService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(AvvisoServiceImpl.class);

    public AvvisoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<AvvisoDTO>>> getAll() {
        log.info("Richiesta recupero avvisi");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/avvisi", webServiceType, headers,
                new ParameterizedTypeReference<List<AvvisoDTO>>() {})
            .doOnNext(response ->
                log.info("Avvisi recuperati: {} elementi", response != null ? response.size() : 0))
            .map(avvisiList -> {
                GenericResponseDTO<List<AvvisoDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(avvisiList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero avvisi: {}", e.getMessage(), e);
                GenericResponseDTO<List<AvvisoDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AvvisoDTO>> getById(Long id) {
        log.info("Richiesta recupero avviso con id: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/avvisi/" + id, webServiceType, headers, AvvisoDTO.class)
            .map(avviso -> {
                GenericResponseDTO<AvvisoDTO> response = new GenericResponseDTO<>();
                response.setData(avviso);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
                return response;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero avviso con id {}: {}", id, e.getMessage(), e);
                GenericResponseDTO<AvvisoDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AvvisoDTO>> create(AvvisoDTO avvisoDTO) {
        log.info("Richiesta creazione avviso");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post("/avvisi", webServiceType, avvisoDTO, headers,
                new ParameterizedTypeReference<AvvisoDTO>() {})
            .map(avviso -> {
                GenericResponseDTO<AvvisoDTO> response = new GenericResponseDTO<>();
                response.setData(avviso);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
                return response;
            })
            .onErrorResume(e -> {
                log.error("Errore creazione avviso: {}", e.getMessage(), e);
                GenericResponseDTO<AvvisoDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AvvisoDTO>> update(Long id, AvvisoDTO avvisoDTO) {
        log.info("Richiesta aggiornamento avviso con id: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.put("/avvisi/" + id, webServiceType, avvisoDTO, headers, AvvisoDTO.class)
            .map(avviso -> {
                GenericResponseDTO<AvvisoDTO> response = new GenericResponseDTO<>();
                response.setData(avviso);
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
                return response;
            })
            .onErrorResume(e -> {
                log.error("Errore aggiornamento avviso con id {}: {}", id, e.getMessage(), e);
                GenericResponseDTO<AvvisoDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> delete(Long id) {
        log.info("Richiesta eliminazione avviso con id: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/avvisi/" + id, webServiceType, headers)
            .then(Mono.fromCallable(() -> {
                GenericResponseDTO<Void> response = new GenericResponseDTO<>();
                response.setStatus(new Status());
                response.getStatus().setSuccess(Boolean.TRUE);
                return response;
            }))
            .onErrorResume(e -> {
                log.error("Errore eliminazione avviso con id {}: {}", id, e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
