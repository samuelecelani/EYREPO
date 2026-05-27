package it.ey.piao.bff.service.impl;

import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.dto.StoricoStatoSezioneDTO;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStoricoStatoSezioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StoricoStatoSezioneServiceImpl implements IStoricoStatoSezioneService {

    private static final Logger log = LoggerFactory.getLogger(StoricoStatoSezioneServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    public StoricoStatoSezioneServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<StoricoStatoSezioneDTO>> save(StoricoStatoSezioneDTO dto) {
        log.info("Salvataggio StoricoStatoSezione per idEntitaFK={} e codTipologiaFK={}",
            dto.getIdEntitaFK(), dto.getCodTipologiaFK());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.<StoricoStatoSezioneDTO>post(
                "/storico-stato-sezione/save",
                webServiceType,
                dto,
                headers,
                new ParameterizedTypeReference<StoricoStatoSezioneDTO>() {}
            )
            .doOnNext(response -> log.info("StoricoStatoSezione salvato con id={}", response.getId()))
            .map(savedEntity -> {
                GenericResponseDTO<StoricoStatoSezioneDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(savedEntity);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio StoricoStatoSezione: {}", e.getMessage());
                GenericResponseDTO<StoricoStatoSezioneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
