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
@Transactional
public class OVPStrategiaServiceImpl implements IOVPStrategiaService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(OVPStrategiaServiceImpl.class);

    public OVPStrategiaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    public Mono<GenericResponseDTO<OVPStrategiaDTO>> save(OVPStrategiaDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/ovp-strategia/save",webServiceType,request,headers, OVPStrategiaDTO.class)
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
    public Mono<GenericResponseDTO<List<OVPStrategiaDTO>>> save(List<OVPStrategiaDTO> request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/ovp-strategia/save",webServiceType,request,headers, new ParameterizedTypeReference<List<OVPStrategiaDTO>>() {})
            .doOnNext(response -> log.info("List<OVPStrategiaDTO> Salvata/Modficata: {}", response))
            .map(strategiaDTOS-> {
                GenericResponseDTO<List<OVPStrategiaDTO>> finalResponse = new GenericResponseDTO<>();
                if (strategiaDTOS == null){
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica List<OVPStrategiaDTO>");
                }
                finalResponse.setData(strategiaDTOS);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica List<OVPStrategiaDTO> {}", e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<OVPStrategiaIndicatoreDTO>> saveIndicatore(OVPStrategiaIndicatoreDTO request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/ovp-strategia/saveIndicatore",webServiceType,request,headers, OVPStrategiaIndicatoreDTO.class)
            .doOnNext(response -> log.info("OVPStrategiaIndicatore Salvata/Modficata: {}", response))
            .map(strategiaIndicatore -> {
                GenericResponseDTO<OVPStrategiaIndicatoreDTO> finalResponse = new GenericResponseDTO<>();
                if (strategiaIndicatore == null){
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica OVPStrategiaIndicatoreDTO");
                }
                finalResponse.setData(strategiaIndicatore);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica OVPStrategiaIndicatoreDTO {}", e);
                GenericResponseDTO<TestDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
}
