package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IOVPStrategiaIndicatoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OVPStrategiaIndicatoreServiceImpl implements IOVPStrategiaIndicatoreService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaIndicatoreServiceImpl.class);

    public OVPStrategiaIndicatoreServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<OVPStrategiaIndicatoreDTO>> saveIndicatore(OVPStrategiaIndicatoreDTO request, Long idStrategia) {
        log.info("Salvataggio OVPStrategiaIndicatore per Strategia con id: {}", idStrategia);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia-indicatore/save/" + idStrategia;
        return webClientService.post(url, webServiceType, request, headers, OVPStrategiaIndicatoreDTO.class)
            .doOnNext(response -> log.info("OVPStrategiaIndicatore Salvato/Modificato: {}", response))
            .map(strategiaIndicatore -> {
                GenericResponseDTO<OVPStrategiaIndicatoreDTO> finalResponse = new GenericResponseDTO<>();
                if (strategiaIndicatore == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica OVPStrategiaIndicatore");
                }
                finalResponse.setData(strategiaIndicatore);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica OVPStrategiaIndicatore: {}", e.getMessage());
                GenericResponseDTO<OVPStrategiaIndicatoreDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteById(Long id) {
        log.info("Richiesta eliminazione OVPStrategiaIndicatore con id: {}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/ovp-strategia-indicatore/delete/" + id;
        return webClientService.delete(url, webServiceType, headers, Void.class)
            .doOnNext(response -> log.info("OVPStrategiaIndicatore con id {} eliminato con successo", id))
            .map(result -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nell'eliminazione di OVPStrategiaIndicatore con id {}: {}", id, e.getMessage());
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
