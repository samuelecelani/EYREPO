package it.ey.piao.bff.service.impl;

import it.ey.dto.AttivitaFormativeDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAttivitaFormativeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AttivitaFormativeServiceImpl implements IAttivitaFormativeService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(AttivitaFormativeServiceImpl.class);

    public AttivitaFormativeServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<AttivitaFormativeDTO>> saveOrUpdate(AttivitaFormativeDTO request)
    {
        log.info("Richiesta salvataggio/modifica AttivitaFormative");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.post("/attivita-formative/save", webServiceType, request, headers, AttivitaFormativeDTO.class)
            .doOnNext(response -> log.info("AttivitaFormative salvato/modificato: {}", response))
            .map(attivitaFormativeDTO -> {
                GenericResponseDTO<AttivitaFormativeDTO> finalResponse = new GenericResponseDTO<>();

                if (attivitaFormativeDTO == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica AttivitaFormative");
                }

                finalResponse.setData(attivitaFormativeDTO);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica AttivitaFormative: {}", e.getMessage(), e);

                GenericResponseDTO<AttivitaFormativeDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione)
    {
        log.info("Richiesta cancellazione AttivitaFormative con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/attivita-formative/" + id + "?");
        if (campiModificati != null && !campiModificati.isBlank()) url.append("campiModificati=").append(campiModificati).append("&");
        if (idPiao != null) url.append("idPiao=").append(idPiao).append("&");
        if (testoSezione != null && !testoSezione.isBlank()) url.append("testoSezione=").append(testoSezione);

        return webClientService.delete(url.toString(), webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("AttivitaFormative con id={} cancellato con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione AttivitaFormative con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }
}
