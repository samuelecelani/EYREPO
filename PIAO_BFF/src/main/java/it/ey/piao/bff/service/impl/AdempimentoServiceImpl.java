package it.ey.piao.bff.service.impl;

import it.ey.dto.AdempimentoDTO;
import it.ey.dto.FaseDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAdempimentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AdempimentoServiceImpl implements IAdempimentoService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(AdempimentoServiceImpl.class);

    public AdempimentoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<AdempimentoDTO>> saveOrUpdateAdempimento(AdempimentoDTO request)
    {
        log.info("Richiesta lista di tutte le funzionalità sulla base del ruolo passato");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post(
                "/adempimento",
                webServiceType,
                request,
                headers,
                AdempimentoDTO.class
            )
            .doOnNext(response -> log.info("Adempimento salvato: {}", request))
            .map(a -> {
                GenericResponseDTO<AdempimentoDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(a);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteAdempimento(Long id)
    {
        log.info("Richiesta di cancellazione adempimento con id: {}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.delete(
                "/adempimento/" + id,
                webServiceType,
                headers,
                Void.class
            )
            .doOnNext(response -> log.info("Adempimento con id {} cancellata con successo", id))
            .map(v -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nella cancellazione dell'adempimento con id {}: {}", id, e.getMessage());
            });
    }
}
