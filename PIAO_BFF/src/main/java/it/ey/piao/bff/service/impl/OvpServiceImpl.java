package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IOVPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OvpServiceImpl implements IOVPService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OvpServiceImpl.class);

    public OvpServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }




    @Override
    public Mono<GenericResponseDTO<OVPDTO>> saveOrUpdate(OVPDTO request) {
        log.info("Salvataggio di un OVP");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/ovp/save", webServiceType, request, headers, OVPDTO.class)
            .doOnNext(response -> log.info("Sezione1 Salvata/Modficata: {}", response))
            .map(ovp -> {
                GenericResponseDTO<OVPDTO> finalResponse = new GenericResponseDTO<>();
                if (ovp == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(ovp);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica OVP {}", e);
                GenericResponseDTO<OVPDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<OVPDTO>>> getOvpByIdSezione21(Long idSezione21) {
        log.info("Recupero di tutti gli ovp");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/ovp/sezione/" + idSezione21, webServiceType, headers, new ParameterizedTypeReference<List<OVPDTO>>() {})
            .doOnNext(response -> log.info(" Numero ovp ricevuti: {}", response.size()))
            .map(ovp -> {
                GenericResponseDTO<List<OVPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(ovp);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero dei dati {}", e);
                GenericResponseDTO<List<OVPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> delete(Long id) {
        log.info("Eliminazione OVP id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.delete("/ovp/" + id, webServiceType, headers, Void.class)
            .then(Mono.fromSupplier(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            }))
            .onErrorResume(e -> {
                log.error("Errore eliminazione OVP id={} -> {}", id, e.getMessage(), e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }
}
