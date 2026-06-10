package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StoricoModificaDTO;
import it.ey.dto.Status;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStoricoModificaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class StoricoModificaServiceImpl implements IStoricoModificaService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(StoricoModificaServiceImpl.class);

    public StoricoModificaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<StoricoModificaDTO>>> findByIdSezioneAndCodTipologiaFK(Long idSezione, Sezione codTipologiaFK) {
        log.info("Richiesta storico modifiche per idSezione: {}, codTipologiaFK: {}", idSezione, codTipologiaFK);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String url = "/storico-modifica?idSezione=" + idSezione + "&codTipologiaFK=" + codTipologiaFK.name();

        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<StoricoModificaDTO>>() {})
            .doOnNext(response -> log.info("Numero storico modifiche ricevute: {}", response.size()))
            .map(storicoList -> {
                GenericResponseDTO<List<StoricoModificaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(storicoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero dello storico modifiche: {}", e.getMessage());
                GenericResponseDTO<List<StoricoModificaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<StoricoModificaDTO>>> findByIdPiao(Long idPiao) {
        log.info("Richiesta storico modifiche per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String url = "/storico-modifica/piao/" + idPiao;

        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<StoricoModificaDTO>>() {})
            .doOnNext(response -> log.info("Numero storico modifiche ricevute per piao: {}", response.size()))
            .map(storicoList -> {
                GenericResponseDTO<List<StoricoModificaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(storicoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero dello storico modifiche per piao: {}", e.getMessage());
                GenericResponseDTO<List<StoricoModificaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
