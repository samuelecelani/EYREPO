package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.AzioniEmail;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import it.ey.piao.bff.service.ISezione31Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class Sezione31ServiceImpl implements ISezione31Service
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private static final Logger log = LoggerFactory.getLogger(Sezione31ServiceImpl.class);

    public Sezione31ServiceImpl(WebClientService webClientService,
                                EmailNotificationHelper emailNotificationHelper)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.emailNotificationHelper = emailNotificationHelper;
    }

    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione31DTO request)
    {
        log.info("Richiesta salvataggio/modifica Sezione31");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione31/save", webServiceType, request, headers, Void.class)
            .doOnNext(response -> log.info("Sezione31 Salvata/Modificata"))
            .map(result -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione31 {}", e.getMessage(), e);
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
        log.info("Richiesta validazione stato Sezione31 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione31/validazione/" + id, webServiceType, new Sezione31DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Modifica Stato Sezione31 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_31, AzioniEmail.RICHIEDI_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione31 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione31DTO>> getOrCreate(PiaoDTO request)
    {
        log.info("Ricerca Sezione31 per idPiao: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/sezione31/piao";
        return webClientService.post(url, webServiceType,request, headers, Sezione31DTO.class)
            .doOnNext(response -> log.info("Sezione31 trovata per idPiao {}: {}", request, response))
            .map(sezione31 -> {
                GenericResponseDTO<Sezione31DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sezione31);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca di Sezione31 per idPiao {}: {}", request, e.getMessage());
                GenericResponseDTO<Sezione31DTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione31DTO>> findByPiao(Long idPiao)
    {
        log.info("Ricerca Sezione31 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione31/"+ idPiao ,webServiceType,headers, Sezione31DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione31-> {
                GenericResponseDTO<Sezione31DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione31 == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione31");
                }
                finalResponse.setData(sezione31);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione31 {}", e);
                GenericResponseDTO<Sezione31DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa) {
        log.info("Validazione Sezione31 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione31/valida-sezione/" + id, webServiceType, new Sezione31DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Valida Sezione31 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_31, AzioniEmail.ACCETTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore valida Sezione31 {}", e.getMessage(), e);
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
        log.info("Rifiuto validazione Sezione31 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione31/rifiuta-validazione/" + id, webServiceType,osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Rifiuta validazione Sezione31 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_31, AzioniEmail.RIFIUTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione Sezione31 {}", e.getMessage(), e);
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
        log.info("Revoca validazione Sezione31 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione31/revoca-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Revoca validazione Sezione31 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_31, AzioniEmail.REVOCA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore revoca validazione Sezione31 {}", e.getMessage(), e);
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
        log.info("Annulla validazione Sezione31 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione31/annulla-validazione/" + id, webServiceType, new Sezione31DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Annulla validazione Sezione31 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_31, AzioniEmail.ANNULLA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore annulla validazione Sezione31 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    /**
     * Mock che emula una chiamata al servizio Minerva per i dati del grafico Sezione 4.
     * Restituisce una lista di coppie key-value simulate.
     */
    @Override
    public Mono<GenericResponseDTO<List<GraficoSezione31DTO>>> getGraficoSezione31Mock() {
        log.info("Mock chiamata servizio Minerva - getGraficoSezione4Mock");

        List<GraficoSezione31DTO> mockData = Arrays.asList(
            GraficoSezione31DTO.builder().key("ISTRUTTORE TECNICO").value("50").build(),
            GraficoSezione31DTO.builder().key("ISTRUTTORE AMMINISTRATIVO").value("30").build(),
            GraficoSezione31DTO.builder().key("ESPERTO/A DI ANALISI DATI").value("20").build()
        );

        GenericResponseDTO<List<GraficoSezione31DTO>> response = new GenericResponseDTO<>();
        response.setData(mockData);
        response.setStatus(new Status());
        response.getStatus().setSuccess(Boolean.TRUE);

        log.info("Mock Minerva - Restituiti {} elementi per il grafico Sezione 4", mockData.size());
        return Mono.just(response);
    }
}
