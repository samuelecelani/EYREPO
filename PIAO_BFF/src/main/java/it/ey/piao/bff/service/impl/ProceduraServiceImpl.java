package it.ey.piao.bff.service.impl;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ProceduraDTO;
import it.ey.dto.Status;
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
import reactor.core.publisher.Mono;

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
}
