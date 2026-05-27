package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.AzioniEmail;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import it.ey.piao.bff.service.ISezione4Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class Sezione4ServiceImpl implements ISezione4Service {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private static final Logger log = LoggerFactory.getLogger(Sezione4ServiceImpl.class);

    public Sezione4ServiceImpl(WebClientService webClientService,
                                EmailNotificationHelper emailNotificationHelper) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.emailNotificationHelper = emailNotificationHelper;
    }

    @Override
    public Mono<GenericResponseDTO<Sezione4DTO>> saveOrUpdate(Sezione4DTO request) {
        log.info("Richiesta salvataggio/modifica Sezione4");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione4/save", webServiceType, request, headers, Sezione4DTO.class)
            .doOnNext(response -> log.info("Sezione4 Salvata/Modificata: {}", response))
            .map(sezione4 -> {
                GenericResponseDTO<Sezione4DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione4 == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(sezione4);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione4 {}", e.getMessage());
                GenericResponseDTO<Sezione4DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa) {
        log.info("Richiesta validazione stato Sezione4 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione4/validazione/" + id, webServiceType, new Sezione4DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Modifica Stato Sezione4 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_4, AzioniEmail.RICHIEDI_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione4 {}", e.getMessage());
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa) {
        log.info("Validazione Sezione4 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione4/valida-sezione/" + id, webServiceType, new Sezione4DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Valida Sezione4 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_4, AzioniEmail.ACCETTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore valida Sezione4 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa) {
        log.info("Rifiuto validazione Sezione4 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione4/rifiuta-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Rifiuta validazione Sezione4 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_4, AzioniEmail.RIFIUTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione Sezione4 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa) {
        log.info("Revoca validazione Sezione4 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione4/revoca-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Revoca validazione Sezione4 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_4, AzioniEmail.REVOCA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore revoca validazione Sezione4 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa) {
        log.info("Annullavalidazione Sezione4 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione4/annulla-validazione/" + id, webServiceType, new Sezione4DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Annulla validazione Sezione4 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_4, AzioniEmail.ANNULLA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore annulla validazione Sezione4 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


   public Mono<GenericResponseDTO<Sezione4DTO>> findByIdPiao(Long idPiao)
    {
        log.info("Ricerca Sezione4 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione4/"+ idPiao ,webServiceType,headers,Sezione4DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione4-> {
                GenericResponseDTO<Sezione4DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione4 == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione");
                }
                finalResponse.setData(sezione4);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione4 {}", e);
                GenericResponseDTO<Sezione4DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });

}

}
