package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IOVPStrategiaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OVPStrategiaServiceImpl implements IOVPStrategiaService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);

    public OVPStrategiaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    public Mono<GenericResponseDTO<OVPStrategiaDTO>> save(OVPStrategiaDTO request, Long idOVP) {
        log.info("Salvataggio OVPStrategia per OVP con id: {}", idOVP);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia/save/" + idOVP;
        return webClientService.post(url, webServiceType, request, headers, OVPStrategiaDTO.class)
            .doOnNext(response -> log.info("OVPStrategia Salvata/Modficata: {}", response))
            .map(strategia-> {
                GenericResponseDTO<OVPStrategiaDTO> finalResponse = new GenericResponseDTO<>();
                if (strategia == null){
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica OVPStrategia");
                }
                finalResponse.setData(strategia);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica OVPStrategiaDTO {}", e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<OVPStrategiaDTO>>> findByOvpId(Long idOvp) {
        log.info("Recupero strategie per OVP con id: {}", idOvp);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia/ovp/" + idOvp;
        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<OVPStrategiaDTO>>() {})
            .doOnNext(response -> log.info("Numero strategie ricevute per OVP {}: {}", idOvp, response.size()))
            .map(strategie -> {
                GenericResponseDTO<List<OVPStrategiaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(strategie);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle strategie per OVP id={}: {}", idOvp, e.getMessage());
                GenericResponseDTO<List<OVPStrategiaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<Void>> delete(Long id) {
        log.info("Richiesta eliminazione OVPStrategia con id: {}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia/delete/" + id;
        return webClientService.delete(url, webServiceType, headers, Void.class)
            .doOnNext(response -> log.info("OVPStrategia con id {} eliminata con successo", id))
            .map(result -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nell'eliminazione di OVPStrategia con id {}: {}", id, e.getMessage());
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

}
