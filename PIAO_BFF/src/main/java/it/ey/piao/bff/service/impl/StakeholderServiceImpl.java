package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStakeholderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
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
    public Mono<Void> deleteById(Long id) {
        // log della richiesta
        log.info("Richiesta cancellazione StakeHolder con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // chiamata DELETE al backend reale
        return webClientService.delete("/stakeholder/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("StakeHolder con id={} cancellato con successo", id))
            .then() // ritorna Mono<Void>
            .onErrorResume(e -> {
                log.error("Errore cancellazione StakeHolder con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

}
