package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IRichiestaApprovazioneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class RichiestaApprovazioneServiceImpl  implements IRichiestaApprovazioneService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(RichiestaApprovazioneServiceImpl.class);

    public RichiestaApprovazioneServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(RichiestaApprovazioneDTO request) {
        log.info("Richiesta salvataggio/modifica RichiestaApprovazione");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/richiesta-approvazione/save", webServiceType, request, headers, Void.class)
            .doOnNext(response -> log.info("RichiestaApprovazione Salvata/Modificata"))
            .map(result -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica RichiestaApprovazione {}", e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<RichiestaApprovazioneDTO>> findByPiao(Long idPiao) {
        log.info("Ricerca RichiestaApprovazione per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/richiesta-approvazione/" + idPiao, webServiceType, headers, RichiestaApprovazioneDTO.class)
            .doOnNext(response -> log.info("Errore nel recupero RichiestaApprovazione: {}", response))
            .map(richiestaApprovazione -> {
                GenericResponseDTO<RichiestaApprovazioneDTO> finalResponse = new GenericResponseDTO<>();
                if (richiestaApprovazione == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero RichiestaApprovazione");
                }
                finalResponse.setData(richiestaApprovazione);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica RichiestaApprovazione {}", e);
                GenericResponseDTO<RichiestaApprovazioneDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }


}
