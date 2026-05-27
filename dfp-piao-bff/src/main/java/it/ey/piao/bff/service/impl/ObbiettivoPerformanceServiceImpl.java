package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.enums.TypeErrorEnum;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IObbiettivoPerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
    public Mono<GenericResponseDTO<Void>> deleteById(
        Long id,
        String campiModificati,
        Long idPiao,
        String testoSezione,
        boolean forceDelete) {

        log.info("Richiesta cancellazione ObiettivoPerformance con id={}, forceDelete={}",
            id, forceDelete);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/obiettivo-performance/" + id + "?");

        if (campiModificati != null && !campiModificati.isBlank()) {
            url.append("campiModificati=")
                .append(URLEncoder.encode(campiModificati, StandardCharsets.UTF_8))
                .append("&");
        }

        if (idPiao != null) {
            url.append("idPiao=").append(idPiao).append("&");
        }

        if (testoSezione != null && !testoSezione.isBlank()) {
            url.append("testoSezione=")
                .append(URLEncoder.encode(testoSezione, StandardCharsets.UTF_8))
                .append("&");
        }

        url.append("forceDelete=").append(forceDelete);

        return webClientService
            .deleteWithExchange(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
            )

            .defaultIfEmpty(new GenericResponseDTO<>())

            .map(apiResponse -> {

                if (apiResponse.getError() != null) {

                    MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                        .value(ObbiettivoPerformanceDTO.class.getSimpleName())   // per FE
                        .idFK(id)
                        .idPiao(idPiao)
                        .build();

                    apiResponse.setMetadato(List.of(metadato));

                    return apiResponse;
                }

                // ⭐ SUCCESS (204 → Mono.empty) → risposta manuale
                GenericResponseDTO<Void> response = new GenericResponseDTO<>();
                Status status = new Status(true);
                response.setStatus(status);

                return response;
            })

            .onErrorResume(e -> {

                log.error("Errore cancellazione ObiettivoPerformance id={} -> {}",
                    id, e.getMessage(), e);

                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                Status status = new Status(false);
                finalResponse.setStatus(status);

                Error err = Error.builder()
                    .messageError(e.getMessage())
                    .errorCode("ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE")
                    .build();

                if (e instanceof WebClientResponseException webEx) {
                    try {
                        GenericResponseDTO<Void> errResp =
                            webEx.getResponseBodyAs(
                                new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
                            );

                        if (errResp != null && errResp.getError() != null) {
                            err = errResp.getError();
                        }

                    } catch (Exception exParse) {
                        log.warn("Errore parsing body ObiettivoPerformance", exParse);
                        err.setMessageError(webEx.getResponseBodyAsString());
                    }
                }

                finalResponse.setError(err);

                MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                    .value(ObbiettivoPerformanceDTO.class.getSimpleName())
                    .idFK(id)
                    .idPiao(idPiao)
                    .build();

                finalResponse.setMetadato(List.of(metadato));

                return Mono.just(finalResponse);
            });
    }
}
