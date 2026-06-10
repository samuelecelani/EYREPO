package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ITabellaFunzionaleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TabellaFunzionaleServiceImpl implements ITabellaFunzionaleService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(TabellaFunzionaleServiceImpl.class);

    public TabellaFunzionaleServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<TabellaFunzionaleDTO>>> findByIdEntitaFKAndCodTipologiaFK(Long idEntitaFK, String codTipologiaFK) {
        log.info("Richiesta lista TabellaFunzionale per codTipologiaFK={}, idEntitaFK={}", codTipologiaFK, idEntitaFK);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/tabella-funzionale/sezione/" + codTipologiaFK + "/" + idEntitaFK, webServiceType, headers,
                new ParameterizedTypeReference<List<TabellaFunzionaleDTO>>() {})
            .doOnNext(response -> log.info("Numero di TabellaFunzionale ricevute: {}", response.size()))
            .map(list -> {
                GenericResponseDTO<List<TabellaFunzionaleDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(list);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero delle TabellaFunzionale per codTipologiaFK={}, idEntitaFK={}: {}", codTipologiaFK, idEntitaFK, e.getMessage(), e);
                GenericResponseDTO<List<TabellaFunzionaleDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<TabellaFunzionaleDTO>> save(TabellaFunzionaleDTO request) {
        log.info("Richiesta salvataggio TabellaFunzionale");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/tabella-funzionale/save", webServiceType, request, headers, TabellaFunzionaleDTO.class)
            .doOnNext(response -> log.info("TabellaFunzionale salvata/modificata: {}", response))
            .map(saved -> {
                GenericResponseDTO<TabellaFunzionaleDTO> finalResponse = new GenericResponseDTO<>();
                if (saved == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(saved);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore salvataggio/modifica TabellaFunzionale: {}", e.getMessage(), e);
                GenericResponseDTO<TabellaFunzionaleDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione) {
        log.info("Richiesta cancellazione TabellaFunzionale con id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        StringBuilder url = new StringBuilder("/tabella-funzionale/" + id + "?");
        if (campiModificati != null && !campiModificati.isBlank()) url.append("campiModificati=").append(campiModificati).append("&");
        if (idPiao != null) url.append("idPiao=").append(idPiao).append("&");
        if (codTipologiaFK != null) url.append("codTipologiaFK=").append(codTipologiaFK).append("&");
        if (testoSezione != null && !testoSezione.isBlank()) url.append("testoSezione=").append(testoSezione);

        return webClientService.delete(url.toString(), webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("TabellaFunzionale con id={} cancellata con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione TabellaFunzionale con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
