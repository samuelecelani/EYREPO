package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.EventoRischioDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.SottofaseMonitoraggioDTO;
import it.ey.dto.Status;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ISottofaseMonitoraggioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class SottofaseMonitoraggioServiceImpl implements ISottofaseMonitoraggioService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(SottofaseMonitoraggioServiceImpl.class);

    public SottofaseMonitoraggioServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<SottofaseMonitoraggioDTO>> saveOrUpdate(SottofaseMonitoraggioDTO request) {
        log.info("Richiesta salvataggio/modifica SottofaseMonitoraggio");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/sottofase-monitoraggio/save", webServiceType, request, headers, SottofaseMonitoraggioDTO.class)
            .doOnNext(response -> log.info("SottofaseMonitoraggio salvato/modificato: {}", response))
            .map(sottofaseMonitoraggioDTO -> {
                GenericResponseDTO<SottofaseMonitoraggioDTO> finalResponse = new GenericResponseDTO<>();

                if (sottofaseMonitoraggioDTO== null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica EventoRischio");
                }

                finalResponse.setData(sottofaseMonitoraggioDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica SottofaseMonitoraggio: {}", e.getMessage(), e);

                GenericResponseDTO<SottofaseMonitoraggioDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<SottofaseMonitoraggioDTO>>> getAllBySezione4(Long idSezione4) {
        log.info("Richiesta recupero SottofaseMonitoraggio per Sezione4 con id={}", idSezione4);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/sottofase-monitoraggio/sezione4/" + idSezione4, webServiceType, headers,
                new ParameterizedTypeReference<List<SottofaseMonitoraggioDTO>>() {})
            .doOnNext(response ->
                log.info("SottofaseMonitoraggio recuperati: {} elementi", response != null ? response.size() : 0))
            .map(sottofaseMonitoraggioDTOList -> {
                GenericResponseDTO<List<SottofaseMonitoraggioDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sottofaseMonitoraggioDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero SottofaseMonitoraggioo per Sezione4 id={}: {}", idSezione4, e.getMessage(), e);

                GenericResponseDTO<List<SottofaseMonitoraggioDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione SottofaseMonitoraggio con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete("/sottofase-monitoraggio/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("SottoFaseMonitoraggio con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione SottofaseMonitoraggio con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
