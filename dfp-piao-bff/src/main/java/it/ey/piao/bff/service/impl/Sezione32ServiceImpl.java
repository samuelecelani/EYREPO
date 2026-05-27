package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.AzioniEmail;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import it.ey.piao.bff.service.ISezione32Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class Sezione32ServiceImpl implements ISezione32Service
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private static final Logger log = LoggerFactory.getLogger(Sezione32ServiceImpl.class);

    public Sezione32ServiceImpl(WebClientService webClientService,
                                EmailNotificationHelper emailNotificationHelper)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.emailNotificationHelper = emailNotificationHelper;
    }

    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione32DTO request)
    {
        log.info("Richiesta salvataggio/modifica Sezione32");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione32/save", webServiceType, request, headers, Void.class)
            .doOnNext(response -> log.info("Sezione32 Salvata/Modificata"))
            .map(result -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione32 {}", e.getMessage(), e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa)
    {
        log.info("Richiesta validazione stato Sezione32 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione32/validazione/" + id, webServiceType, new Sezione32DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Modifica Stato Sezione32 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_32, AzioniEmail.RICHIEDI_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione32 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione32DTO>> getOrCreate(PiaoDTO request)
    {
        log.info("Ricerca Sezione32 per idPiao: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/sezione32/piao";
        return webClientService.post(url, webServiceType,request, headers, Sezione32DTO.class)
            .doOnNext(response -> log.info("Sezione32 trovata per idPiao {}: {}", request, response))
            .map(sezione32 -> {
                GenericResponseDTO<Sezione32DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sezione32);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca di Sezione32 per idPiao {}: {}", request, e.getMessage());
                GenericResponseDTO<Sezione32DTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione32DTO>> findByPiao(Long idPiao)
    {
        log.info("Ricerca Sezione32 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione32/"+ idPiao ,webServiceType,headers, Sezione32DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione32: {}", response))
            .map(sezione32-> {
                GenericResponseDTO<Sezione32DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione32 == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione32");
                }
                finalResponse.setData(sezione32);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione32 {}", e);
                GenericResponseDTO<Sezione32DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa)
    {
        log.info("Validazione Sezione32 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione32/valida-sezione/" + id, webServiceType, new Sezione32DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Valida Sezione32 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_32, AzioniEmail.ACCETTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore valida Sezione32 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa)
    {
        log.info("Rifiuto validazione Sezione32 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione32/rifiuta-validazione/" + id, webServiceType,osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Rifiuta validazione Sezione32 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_32, AzioniEmail.RIFIUTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione Sezione32 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa)
    {
        log.info("Revoca validazione Sezione32 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione32/revoca-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Revoca validazione Sezione32 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_32, AzioniEmail.REVOCA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore revoca validazione Sezione32 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa)
    {
        log.info("Annulla validazione Sezione32 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione32/annulla-validazione/" + id, webServiceType, new Sezione32DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Annulla validazione Sezione32 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_32, AzioniEmail.ANNULLA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore annulla validazione Sezione32 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
