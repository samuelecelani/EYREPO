package it.ey.piao.bff.service.impl;

import it.ey.dto.AnagraficaDTO;
import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAnagraficaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AnagraficaServiceImpl implements IAnagraficaService {

    private static final Logger log = LoggerFactory.getLogger(AnagraficaServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    public AnagraficaServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<AnagraficaDTO>>> getAll() {
        log.info("Richiesta recupero Anagrafiche");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/anagrafica", webServiceType, headers,
                new ParameterizedTypeReference<List<AnagraficaDTO>>() {})
            .doOnNext(response ->
                log.info("Anagrafiche recuperate: {} elementi",
                    response != null ? response.size() : 0))
            .map(dtoList -> {
                GenericResponseDTO<List<AnagraficaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(dtoList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero Anagrafiche: {}", e.getMessage(), e);
                GenericResponseDTO<List<AnagraficaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AnagraficaDTO>> save(AnagraficaDTO anagraficaDTO) {
        log.info("Richiesta salvataggio Anagrafica");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");

        return webClientService.post("/anagrafica/save", webServiceType, anagraficaDTO, headers, AnagraficaDTO.class)
            .doOnNext(response -> log.info("Anagrafica salvata: {}", response))
            .map(dto -> {
                GenericResponseDTO<AnagraficaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(dto);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio Anagrafica: {}", e.getMessage(), e);
                GenericResponseDTO<AnagraficaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}

