package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.TypeErrorEnum;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStakeholderService;
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
import java.util.Map;

@Service
public class StakeholderServiceImpl implements IStakeholderService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(StakeholderServiceImpl.class);

    public StakeholderServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<List<StakeHolderDTO>>> findByidPiao(Long idPiao) {
        log.info("Recupero la lista degli stakeholder");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/stakeholder/piao/"+ idPiao, webServiceType,headers,new ParameterizedTypeReference<List<StakeHolderDTO>>() {})
            .doOnNext(response -> log.info(" Numero di stakeholder ricevuti: {}", response.size()))
            .map(s -> {
                GenericResponseDTO<List<StakeHolderDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(s);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<StakeHolderDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<StakeHolderDTO>> save(StakeHolderDTO request) {
        log.info("Salvataggio stakeholder");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/stakeholder/save",webServiceType,request,headers, StakeHolderDTO.class)
            .doOnNext(response -> log.info("stakeholder Salvata/Modficata: {}", response))
            .map(s-> {
                GenericResponseDTO<StakeHolderDTO> finalResponse = new GenericResponseDTO<>();
                if (s == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(s);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica stakeholder {}", e);
                GenericResponseDTO<StakeHolderDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, boolean forceDelete) {

        log.info("Richiesta cancellazione Stakeholder con id={}, forceDelete={}", id, forceDelete);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/stakeholder/" + id + "?");

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

        return webClientService.deleteWithExchange(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<GenericResponseDTO<Void>>() {}
            )
            .defaultIfEmpty(new GenericResponseDTO<>())

            .map(apiResponse -> {

                if (apiResponse != null && apiResponse.getError() != null) {

                    MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                        .value(StakeHolderDTO.class.getSimpleName())
                        .idFK(id)
                        .idPiao(idPiao)
                        .build();

                    apiResponse.setMetadato(List.of(metadato));

                    return apiResponse;
                }

                GenericResponseDTO<Void> successResponse = new GenericResponseDTO<>();
                Status status = new Status();
                status.setSuccess(true);
                successResponse.setStatus(status);

                return successResponse;
            })

            .onErrorResume(e -> {

                log.error("Errore cancellazione Stakeholder id={}: {}", id, e.getMessage(), e);

                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                Status status = new Status();
                status.setSuccess(false);
                finalResponse.setStatus(status);

                Error err = Error.builder()
                    .messageError(e.getMessage())
                    .errorCode("ERRORE_INTERNO_CONFLITTI_CANCELLAZIONE")
                    .build();

                if (e instanceof WebClientResponseException webEx) {
                    try {
                        GenericResponseDTO<Void> errorResponse =
                            webEx.getResponseBodyAs(new ParameterizedTypeReference<GenericResponseDTO<Void>>() {});

                        if (errorResponse != null && errorResponse.getError() != null) {
                            err = errorResponse.getError();
                        }

                    } catch (Exception exParse) {
                        log.warn("Errore parsing body risposta Stakeholder", exParse);
                        err.setMessageError(webEx.getResponseBodyAsString());
                    }
                }

                finalResponse.setError(err);

                MetadatoDTO<Object> metadato = MetadatoDTO.<Object>builder()
                    .value(StakeHolderDTO.class.getSimpleName())
                    .idFK(id)
                    .idPiao(idPiao)
                    .build();

                finalResponse.setMetadato(List.of(metadato));

                return Mono.just(finalResponse);
            });
    }

}
