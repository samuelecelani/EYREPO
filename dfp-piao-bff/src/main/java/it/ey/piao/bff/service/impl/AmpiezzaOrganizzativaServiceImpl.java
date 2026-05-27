package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAmpiezzaOrganizzativaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AmpiezzaOrganizzativaServiceImpl implements IAmpiezzaOrganizzativaService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(AmpiezzaOrganizzativaServiceImpl.class);

    public AmpiezzaOrganizzativaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<AmpiezzaOrganizzativaDTO>>> findByIdSezione31(Long idSezione31) {
        log.info("Richiesta lista AmpiezzaOrganizzativa per idSezione31={}", idSezione31);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/ampiezza-organizzativa/sezione/" + idSezione31, webServiceType, headers,
                new ParameterizedTypeReference<List<AmpiezzaOrganizzativaDTO>>() {})
            .doOnNext(response -> log.info("Numero di AmpiezzaOrganizzativa ricevute: {}", response.size()))
            .map(list -> {
                GenericResponseDTO<List<AmpiezzaOrganizzativaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(list);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle AmpiezzaOrganizzativa per idSezione31={}: {}", idSezione31, e.getMessage(), e);
                GenericResponseDTO<List<AmpiezzaOrganizzativaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AmpiezzaOrganizzativaDTO>> save(AmpiezzaOrganizzativaDTO request) {
        log.info("Richiesta salvataggio AmpiezzaOrganizzativa");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/ampiezza-organizzativa/save", webServiceType, request, headers, AmpiezzaOrganizzativaDTO.class)
            .doOnNext(response -> log.info("AmpiezzaOrganizzativa salvata/modificata: {}", response))
            .map(saved -> {
                GenericResponseDTO<AmpiezzaOrganizzativaDTO> finalResponse = new GenericResponseDTO<>();
                if (saved == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(saved);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore salvataggio/modifica AmpiezzaOrganizzativa: {}", e.getMessage(), e);
                GenericResponseDTO<AmpiezzaOrganizzativaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione) {
        log.info("Richiesta cancellazione AmpiezzaOrganizzativa con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        StringBuilder url = new StringBuilder("/ampiezza-organizzativa/" + id + "?");
        if (campiModificati != null && !campiModificati.isBlank()) url.append("campiModificati=").append(campiModificati).append("&");
        if (idPiao != null) url.append("idPiao=").append(idPiao).append("&");
        if (testoSezione != null && !testoSezione.isBlank()) url.append("testoSezione=").append(testoSezione);
        return webClientService.delete(url.toString(), webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("AmpiezzaOrganizzativa con id={} cancellata con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione AmpiezzaOrganizzativa con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
