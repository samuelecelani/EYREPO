package it.ey.piao.bff.service.impl;

import it.ey.dto.DatiPubblicatiDTO;
import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.dto.Status;
import it.ey.entity.DatiPubblicati;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IDatiPubblicatiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class DatiPubblicatiServiceImpl implements IDatiPubblicatiService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(DatiPubblicatiServiceImpl.class);

    public DatiPubblicatiServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<DatiPubblicatiDTO>> saveOrUpdate(DatiPubblicatiDTO request) {
        log.info("Richiesta salvataggio/modifica DatiPubblicati");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/dati-pubblicati/save", webServiceType, request, headers, DatiPubblicatiDTO.class)
            .doOnNext(response -> log.info("DatiPubblicati Salvato/Modificato: {}", response))
            .map(datiPubblicati -> {
                GenericResponseDTO<DatiPubblicatiDTO> finalResponse = new GenericResponseDTO<>();
                if (datiPubblicati== null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(datiPubblicati);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica DatiPubblicati: {}", e.getMessage(), e);
                GenericResponseDTO<DatiPubblicatiDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<DatiPubblicatiDTO>>> getAllByObbligoLegge(Long idObbligoLegge) {
        log.info("Richiesta recupero DatiPubblicati per Sezione22 con id={}", idObbligoLegge);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/dati-pubblicati/obbligo-legge/" + idObbligoLegge, webServiceType, headers,
                new ParameterizedTypeReference<List<DatiPubblicatiDTO>>() {})
            .doOnNext(response -> log.info("DatiPubblicatirecuperati: {} elementi", response != null ? response.size() : 0))
            .map(datiPubblicatii -> {
                GenericResponseDTO<List<DatiPubblicatiDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(datiPubblicatii);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero DatiPubblicati per ObbligoLegge id={}: {}", idObbligoLegge, e.getMessage(), e);
                GenericResponseDTO<List<DatiPubblicatiDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione DatiPubblicaticon id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.delete("/dati-pubblicati/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("DatiPubblicati con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione DatiPubblicati con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
