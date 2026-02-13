package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IMonitoraggioPrevenzioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MonitoraggioPrevenzioneServiceImpl implements IMonitoraggioPrevenzioneService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(MonitoraggioPrevenzioneServiceImpl.class);

    public MonitoraggioPrevenzioneServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<MonitoraggioPrevenzioneDTO>> saveOrUpdate(MonitoraggioPrevenzioneDTO request)
    {
        log.info("Richiesta salvataggio/modifica MonitoraggioPrevenzione");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/monitoraggio-prevenzione/save", webServiceType, request, headers, MonitoraggioPrevenzioneDTO.class)
            .doOnNext(response -> log.info("MonitoraggioPrevenzione salvato/modificato: {}", response))
            .map(monitoraggioPrevenzioneDTO -> {
                GenericResponseDTO<MonitoraggioPrevenzioneDTO> finalResponse = new GenericResponseDTO<>();

                if (monitoraggioPrevenzioneDTO == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica MonitoraggioPrevenzione");
                }

                finalResponse.setData(monitoraggioPrevenzioneDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica MonitoraggioPrevenzione: {}", e.getMessage(), e);

                GenericResponseDTO<MonitoraggioPrevenzioneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<MonitoraggioPrevenzioneDTO>>> getAllByMisuraPrevenzioneEventoRischio(Long idMisuraPrevenzioneEventoRischio)
    {
        log.info("Richiesta recupero MonitoraggioPrevenzione per MisuraPrevenzioneEventoRischio con id={}", idMisuraPrevenzioneEventoRischio);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/monitoraggio-prevenzione/misura-prevenzione-evento-rischio/" + idMisuraPrevenzioneEventoRischio, webServiceType, headers,
                new ParameterizedTypeReference<List<MonitoraggioPrevenzioneDTO>>() {})
            .doOnNext(response ->
                log.info("EventoRischio recuperati: {} elementi", response != null ? response.size() : 0))
            .map(monitoraggioPrevenzioneDTOList -> {
                GenericResponseDTO<List<MonitoraggioPrevenzioneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(monitoraggioPrevenzioneDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero MonitoraggioPrevenzione per MisuraPrevenzioneEventoRischio id={}: {}", idMisuraPrevenzioneEventoRischio, e.getMessage(), e);

                GenericResponseDTO<List<MonitoraggioPrevenzioneDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id)
    {
        log.info("Richiesta cancellazione MonitoraggioPrevenzione con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/monitoraggio-prevenzione/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("MonitoraggioPrevenzione con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione MonitoraggioPrevenzione con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

}
