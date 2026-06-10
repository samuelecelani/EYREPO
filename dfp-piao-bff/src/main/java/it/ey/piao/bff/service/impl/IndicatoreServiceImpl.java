package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IIndicatoreService;
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
public class IndicatoreServiceImpl implements IIndicatoreService {

    private static final Logger log = LoggerFactory.getLogger(IndicatoreServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    public IndicatoreServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<IndicatoreDTO>> saveOrUpdate(IndicatoreDTO request) {
        log.info("Salvataggio Indicatore");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/indicatore/save",webServiceType,request,headers, IndicatoreDTO.class)
            .doOnNext(response -> log.info("Indicatore Salvata/Modficata: {}", response))
            .map(s-> {
                GenericResponseDTO<IndicatoreDTO> finalResponse = new GenericResponseDTO<>();
                if (s == null){
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(s);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Indicatore {}", e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<IndicatoreDTO>>> findBy(Long idPiao, Long idEntitaFK, String codTipologiaFK) {
        log.info("Ricerca Indicatore per idPiao: {}, idEntitaFK: {}, codTipologiaFK: {}", idPiao, idEntitaFK, codTipologiaFK);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/indicatore?idPiao=" + idPiao + "&idEntitaFK=" + idEntitaFK + "&codTipologiaFK=" + codTipologiaFK;
        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<IndicatoreDTO>>() {})
            .doOnNext(response -> log.info("Numero Indicatori trovati: {}", response.size()))
            .map(indicatoriList -> {
                GenericResponseDTO<List<IndicatoreDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(indicatoriList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca degli Indicatori: {}", e.getMessage());
                GenericResponseDTO<List<IndicatoreDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione) {

        log.info("Richiesta cancellazione Indicatore con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/indicatore/" + id + "?");

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

        // Rimuove eventuale '&' o '?' finale
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
                        .value(IndicatoreDTO.class.getSimpleName())
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

                log.error("Errore cancellazione Indicatore id={}: {}", id, e.getMessage(), e);

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
                        log.warn("Errore parsing body risposta Indicatore", exParse);
                        err.setMessageError(webEx.getResponseBodyAsString());
                    }
                }

                finalResponse.setError(err);

                MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                    .value(IndicatoreDTO.class.getSimpleName())
                    .idFK(id)
                    .idPiao(idPiao)
                    .build();

                finalResponse.setMetadato(List.of(metadato));

                return Mono.just(finalResponse);
            });
    }
}
