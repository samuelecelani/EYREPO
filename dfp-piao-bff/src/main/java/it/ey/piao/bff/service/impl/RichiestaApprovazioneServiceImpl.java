package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IPiaoService;
import it.ey.piao.bff.service.IRichiestaApprovazioneService;
import it.ey.piao.bff.service.IUserService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class RichiestaApprovazioneServiceImpl  implements IRichiestaApprovazioneService {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private final IUserService userService;
    private final IPiaoService piaoService;
    private static final Logger log = LoggerFactory.getLogger(RichiestaApprovazioneServiceImpl.class);

    public RichiestaApprovazioneServiceImpl(WebClientService webClientService,
                                            EmailNotificationHelper emailNotificationHelper,
                                            IUserService userService,
                                            IPiaoService piaoService) {
        this.webClientService = webClientService;
        this.emailNotificationHelper = emailNotificationHelper;
        this.userService = userService;
        this.piaoService = piaoService;
        this.webServiceType = WebServiceType.API;
    }


    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(RichiestaApprovazioneDTO request) {
        log.info("Richiesta salvataggio/modifica RichiestaApprovazione");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/richiesta-approvazione/save", webServiceType, request, headers, Void.class)
            .doOnNext(response -> log.info("RichiestaApprovazione Salvata/Modificata"))
            .then(Mono.defer(() -> resolveCcEmail(request.getIdPiao())))
            .flatMap(ccEmail -> emailNotificationHelper.sendEmailApprovazione(
                request.getMail(),
                request.getIdPiao(),
                ccEmail.isBlank() ? null : ccEmail))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Salvataggio/modifica RichiestaApprovazione {}", e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    /**
     * Recupera l'email dell'utente loggato filtrando la sua lista di {@code paRiferimento}
     * per il {@code codPAFK} del PIAO indicato. Restituisce stringa vuota se non disponibile.
     */
    private Mono<String> resolveCcEmail(Long idPiao) {
        if (idPiao == null) {
            return Mono.just("");
        }
        return Mono.zip(userService.getUserbyToken(), piaoService.findById(idPiao))
            .map(tuple -> {
                GenericResponseDTO<UserDTO> userResp = tuple.getT1();
                GenericResponseDTO<PiaoDTO> piaoResp = tuple.getT2();
                if (userResp == null || userResp.getData() == null
                    || piaoResp == null || piaoResp.getData() == null) {
                    return "";
                }
                UserDTO user = userResp.getData();
                String codPa = piaoResp.getData().getCodPAFK();
                if (codPa == null || user.getPaRiferimento() == null) {
                    return "";
                }
                return user.getPaRiferimento().stream()
                    .filter(pa -> codPa.equals(pa.getCodePA()))
                    .map(PaRiferimentoDTO::getEmail)
                    .filter(e -> e != null && !e.isBlank())
                    .findFirst()
                    .orElse("");
            })
            .onErrorResume(e -> {
                log.warn("Impossibile determinare l'email CC utente loggato per idPiao={}: {}", idPiao, e.getMessage());
                return Mono.just("");
            })
            .defaultIfEmpty("");
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
