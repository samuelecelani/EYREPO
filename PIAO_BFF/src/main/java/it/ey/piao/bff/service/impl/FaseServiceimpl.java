package it.ey.piao.bff.service.impl;

import it.ey.dto.FaseDTO;
import it.ey.dto.FunzionalitaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IFaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;



@Service
public class FaseServiceimpl implements IFaseService {


    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(FaseServiceimpl.class);

    public FaseServiceimpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<FaseDTO>> saveOrUpdateFase(FaseDTO request) {
        log.info("Richiesta lista di tutte le funzionalità sulla base del ruolo passato");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post(
                "/fase",
                webServiceType,
                request,
                headers,
                FaseDTO.class
            )
            .doOnNext(response -> log.info("Fase salvata: {}", request))
            .map(f -> {
                GenericResponseDTO<FaseDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(f);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteFase(Long id) {
        log.info("Richiesta di cancellazione fase con id: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete(
                "/fase/" + id,
                webServiceType,
                headers,
                Void.class
            )
            .doOnNext(response -> log.info("Fase con id {} cancellata con successo", id))
            .map(v -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nella cancellazione della fase con id {}: {}", id, e.getMessage());
            });
    }
}
