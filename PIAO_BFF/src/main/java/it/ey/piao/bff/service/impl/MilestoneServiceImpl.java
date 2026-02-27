package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IMilestoneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class MilestoneServiceImpl implements IMilestoneService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(MilestoneServiceImpl.class);

    public MilestoneServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<MilestoneDTO>> saveOrUpdate(MilestoneDTO request)
    {
        log.info("Richiesta salvataggio/modifica Milestone");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/milestone/save", webServiceType, request, headers, MilestoneDTO.class)
            .doOnNext(response -> log.info("Milestone salvato/modificato: {}", response))
            .map(milestoneDTO -> {
                GenericResponseDTO<MilestoneDTO> finalResponse = new GenericResponseDTO<>();

                if (milestoneDTO == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica Milestone");
                }

                finalResponse.setData(milestoneDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica Milestone: {}", e.getMessage(), e);

                GenericResponseDTO<MilestoneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PromemoriaDTO>>> getPromemoriaByMilestone(Long id)
    {
        log.info("Richiesta recupero Promemoria per Milestone con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/milestone/promemoria/" + id, webServiceType, headers,
                new ParameterizedTypeReference<List<PromemoriaDTO>>() {})
            .doOnNext(response ->
                log.info("Promemoria recuperati: {} elementi", response != null ? response.size() : 0))
            .map(promemoriaDTOList -> {
                GenericResponseDTO<List<PromemoriaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(promemoriaDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero Promemoria per Milestone id={}: {}", id, e.getMessage(), e);

                GenericResponseDTO<List<PromemoriaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<MilestoneDTO>>> getMilestoneBySottofaseMonitoraggio(Long idSottofaseMonitoraggio)
    {
        log.info("Richiesta recupero Milestone per SottofaseMonitoraggio con id={}", idSottofaseMonitoraggio);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/milestone/sottofase-monitoraggio/" + idSottofaseMonitoraggio, webServiceType, headers,
                new ParameterizedTypeReference<List<MilestoneDTO>>() {})
            .doOnNext(response ->
                log.info("Milestone recuperati: {} elementi", response != null ? response.size() : 0))
            .map(milestoneDTOList -> {
                GenericResponseDTO<List<MilestoneDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(milestoneDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero Milestone per SottofaseMonitoraggio id={}: {}", idSottofaseMonitoraggio, e.getMessage(), e);

                GenericResponseDTO<List<MilestoneDTO>> errorResponse = new GenericResponseDTO<>();
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
        log.info("Richiesta cancellazione Milestone con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/milestone/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("Milestone con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione Milestone con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
