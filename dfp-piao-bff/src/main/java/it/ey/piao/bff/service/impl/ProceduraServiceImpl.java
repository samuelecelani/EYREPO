package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.dto.ProceduraDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IProceduraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ProceduraServiceImpl implements IProceduraService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(ProceduraServiceImpl.class);

    public ProceduraServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<ProceduraDTO>>> getProcedure(Long idSezione1) {
        log.info("Recupero la lista delle procedure");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/procedura/sezione/"+ idSezione1, webServiceType, headers, new ParameterizedTypeReference<List<ProceduraDTO>>() {})
            .doOnNext(response -> log.info("Numero di procedure ricevute: {}", response.size()))
            .map(proceduraList -> {
                GenericResponseDTO<List<ProceduraDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(proceduraList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<ProceduraDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<ProceduraDTO>> save(ProceduraDTO request) {
        log.info("Salvataggio procedura");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/procedura/save", webServiceType, request, headers, ProceduraDTO.class)
            .doOnNext(response -> log.info("Procedura Salvata/Modificata: {}", response))
            .map(procedura -> {
                GenericResponseDTO<ProceduraDTO> finalResponse = new GenericResponseDTO<>();
                if (procedura == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(procedura);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica procedura {}", e);
                GenericResponseDTO<ProceduraDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }


        @Override
        public Mono<GenericResponseDTO<Void>> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione) {

            log.info("Richiesta cancellazione Procedura con id={}", id);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");

            StringBuilder url = new StringBuilder("/procedura/" + id + "?");

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

            if (url.charAt(url.length() - 1) == '&' || url.charAt(url.length() - 1) == '?') {
                url.deleteCharAt(url.length() - 1);
            }

            return webClientService
                .deleteWithExchange(
                    url.toString(),
                    webServiceType,
                    headers,
                    new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
                )
                .defaultIfEmpty(new GenericResponseDTO<>())
                .map(apiResponse -> {

                    if (apiResponse != null && apiResponse.getError() != null) {

                        MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                            .value(ProceduraDTO.class.getSimpleName())
                            .idFK(id)
                            .idPiao(idPiao)
                            .build();

                        apiResponse.setMetadato(List.of(metadato));
                        return apiResponse;
                    }

                    GenericResponseDTO<Void> successResponse = new GenericResponseDTO<>();
                    successResponse.setStatus(new Status(true));
                    return successResponse;
                })
                .onErrorResume(e -> {

                    log.error("Errore cancellazione Procedura id={}: {}", id, e.getMessage(), e);

                    GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                    finalResponse.setStatus(new Status(false));

                    Error err = Error.builder()
                        .messageError(e.getMessage())
                        .errorCode("ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE")
                        .build();

                    if (e instanceof WebClientResponseException webEx) {
                        try {
                            GenericResponseDTO<Void> errorResponse = webEx.getResponseBodyAs(
                                new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
                            );

                            if (errorResponse != null && errorResponse.getError() != null) {
                                err = errorResponse.getError();
                            }

                        } catch (Exception exParse) {
                            log.warn("Errore parsing body risposta Procedura", exParse);
                            err.setMessageError(webEx.getResponseBodyAsString());
                        }
                    }

                    finalResponse.setError(err);

                    MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                        .value(ProceduraDTO.class.getSimpleName())
                        .idFK(id)
                        .idPiao(idPiao)
                        .build();

                    finalResponse.setMetadato(List.of(metadato));

                    return Mono.just(finalResponse);
                });
        }
    }

