package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IObbiettivoPerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ObbiettivoPerformanceServiceImpl implements IObbiettivoPerformanceService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(ObbiettivoPerformanceServiceImpl.class);

    public ObbiettivoPerformanceServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<ObbiettivoPerformanceDTO>> saveOrUpdate(ObbiettivoPerformanceDTO request) {
        log.info("Richiesta salvataggio/modifica ObbiettivoPerformance");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/obiettivo-performance/save", webServiceType, request, headers, ObbiettivoPerformanceDTO.class)
            .doOnNext(response -> log.info("ObbiettivoPerformance Salvato/Modificato: {}", response))
            .map(obiettivo -> {
                GenericResponseDTO<ObbiettivoPerformanceDTO> finalResponse = new GenericResponseDTO<>();
                if (obiettivo == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(obiettivo);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica ObbiettivoPerformance: {}", e.getMessage(), e);
                GenericResponseDTO<ObbiettivoPerformanceDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>> getAllBySezione22(Long idSezione22) {
        log.info("Richiesta recupero ObiettiviPerformance per Sezione22 con id={}", idSezione22);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/obiettivo-performance/sezione22/" + idSezione22, webServiceType, headers,
                new ParameterizedTypeReference<List<ObbiettivoPerformanceDTO>>() {})
            .doOnNext(response -> log.info("ObiettiviPerformance recuperati: {} elementi", response != null ? response.size() : 0))
            .map(obiettivi -> {
                GenericResponseDTO<List<ObbiettivoPerformanceDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obiettivi);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero ObiettiviPerformance per Sezione22 id={}: {}", idSezione22, e.getMessage(), e);
                GenericResponseDTO<List<ObbiettivoPerformanceDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>> findByTipologiaAndFilters(TipologiaObbiettivo tipologia, Long idOvp, Long idStrategia) {
        log.info("Richiesta recupero ObiettiviPerformance con tipologia={}, idOvp={}, idStrategia={}", tipologia, idOvp, idStrategia);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // Costruisco l'URL con i parametri query
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath("/obiettivo-performance/filter")
            .queryParam("tipologia", tipologia.name());
        if (idOvp != null) {
            uriBuilder.queryParam("idOvp", idOvp);
        }
        if (idStrategia != null) {
            uriBuilder.queryParam("idStrategia", idStrategia);
        }
        String url = uriBuilder.build().toUriString();

        return webClientService.get(url, webServiceType, headers,
                new ParameterizedTypeReference<List<ObbiettivoPerformanceDTO>>() {})
            .doOnNext(response -> log.info("ObiettiviPerformance recuperati con filtri: {} elementi", response != null ? response.size() : 0))
            .map(obiettivi -> {
                GenericResponseDTO<List<ObbiettivoPerformanceDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(obiettivi);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero ObiettiviPerformance con filtri: {}", e.getMessage(), e);
                GenericResponseDTO<List<ObbiettivoPerformanceDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        log.info("Richiesta cancellazione ObbiettivoPerformance con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.delete("/obiettivo-performance/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("ObbiettivoPerformance con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione ObbiettivoPerformance con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
