package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAttoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AttoreServiceImpl implements IAttoreService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(AttoreServiceImpl.class);

    public AttoreServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<AttoreDTO>> save(Long idPiao, AttoreDTO attore) {
        log.info("Salvataggio attore per idPiao={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.<AttoreDTO>post(
                "/attore/piao/" + idPiao + "/save",
                webServiceType,
                attore,
                headers,
                new ParameterizedTypeReference<AttoreDTO>() {}
            )
            .doOnNext(response -> log.info("Salvato attore per idPiao={}", idPiao))
            .map(savedAttore -> {
                GenericResponseDTO<AttoreDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(savedAttore);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio attore per idPiao={}: {}", idPiao, e.getMessage());
                GenericResponseDTO<AttoreDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<AttoreDTO>>> findListByIdPiao(Long idPiao) {
        log.info("Richiesta lista attori per idPiao={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/attore/piao/" + idPiao ,
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<AttoreDTO>>() {}
            )
            .doOnNext(response -> log.info("Trovati {} attori per idPiao={}", response != null ? response.size() : 0, idPiao))
            .map(attori -> {
                GenericResponseDTO<List<AttoreDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(attori);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero lista attori per idPiao={}: {}", idPiao, e.getMessage());
                GenericResponseDTO<List<AttoreDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AttoreDTO>> findByExternalIdAndTipoSezione(Long externalId, Sezione tipoSezione) {
        log.info("Richiesta attore per externalId={} e tipoSezione={}", externalId, tipoSezione);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/attore/external/" + externalId + "/sezione/" + tipoSezione.name(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<AttoreDTO>() {}
            )
            .doOnNext(response -> log.info("Trovato attore per externalId={} e tipoSezione={}", externalId, tipoSezione))
            .map(attore -> {
                GenericResponseDTO<AttoreDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(attore);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero attore per externalId={}, tipoSezione={}: {}", externalId, tipoSezione, e.getMessage());
                GenericResponseDTO<AttoreDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
