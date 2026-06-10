package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.AzioniEmail;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import it.ey.piao.bff.service.ISezione332Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class Sezione332ServiceImpl implements ISezione332Service
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private static final Logger log = LoggerFactory.getLogger(Sezione332ServiceImpl.class);

    public Sezione332ServiceImpl(WebClientService webClientService,
                                EmailNotificationHelper emailNotificationHelper)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.emailNotificationHelper = emailNotificationHelper;
    }


    @Override
    public Mono<GenericResponseDTO<Sezione332DTO>> getOrCreate(PiaoDTO request)
    {
        log.info("Ricerca Sezione332 per idPiao: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/sezione332/piao";
        return webClientService.post(url, webServiceType, request, headers, Sezione332DTO.class)
            .doOnNext(response -> log.info("Sezione332 trovata per idPiao {}: {}", request, response))
            .map(sezione332 -> {
                GenericResponseDTO<Sezione332DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sezione332);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca di Sezione332 per idPiao {}: {}", request, e.getMessage());
                GenericResponseDTO<Sezione332DTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<Sezione332DTO>> saveOrUpdate(Sezione332DTO request) {
        log.info("Richiesta salvataggio/aggiornamento Sezione332");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione332/save", webServiceType, request, headers, Sezione332DTO.class)
            .doOnNext(response -> log.info("Sezione332 Salvata/Modificata: {}", response))
            .map(sezione332 -> {
                GenericResponseDTO<Sezione332DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione332 == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(sezione332);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione332 {}", e);
                GenericResponseDTO<Sezione332DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new it.ey.dto.Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa) {
        log.info("Richiesta validazione stato Sezione332 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione332/validazione/" + id, webServiceType, new Sezione332DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Modifica Stato Sezione332 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_332, AzioniEmail.RICHIEDI_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione332 {}", e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione332DTO>> findByPiao(Long idPiao) {
        log.info("Ricerca Sezione332 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione332/" + idPiao, webServiceType, headers, Sezione332DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione332 -> {
                GenericResponseDTO<Sezione332DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione332 == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione");
                }
                finalResponse.setData(sezione332);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione332 {}", e);
                GenericResponseDTO<Sezione332DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new it.ey.dto.Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa) {
        log.info("Validazione Sezione332 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione332/valida-sezione/" + id, webServiceType, new Sezione332DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Valida Sezione332 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_332, AzioniEmail.ACCETTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore valida Sezione332 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long id, String osservazioni, String codicePa) {
        log.info("Rifiuto validazione Sezione332 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione332/rifiuta-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Rifiuta validazione Sezione332 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_332, AzioniEmail.RIFIUTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione Sezione332 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> revocaValidazione(Long id, String osservazioni, String codicePa) {
        log.info("Revoca validazione Sezione332 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione332/revoca-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Revoca validazione Sezione332 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_332, AzioniEmail.REVOCA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore revoca validazione Sezione332 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new it.ey.dto.Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> annullaValidazione(Long id, String codicePa) {
        log.info("Annullo validazione Sezione332 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione332/annulla-validazione/" + id, webServiceType, new Sezione332DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Annulla validazione Sezione332 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_332, AzioniEmail.ANNULLA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore annulla validazione Sezione332 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<TipologiaAttivitaDTO>>> getTipologiaAttivita()
    {
        log.info("Richiesta recupero TipologiaAttivita");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/sezione332/tipologia-attivita", webServiceType, headers,
                new ParameterizedTypeReference<List<TipologiaAttivitaDTO>>() {})
            .doOnNext(response ->
                log.info("TipologiaAttivita recuperati: {} elementi", response != null ? response.size() : 0))
            .map(tipologiaAttivitaDTOList -> {
                GenericResponseDTO<List<TipologiaAttivitaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(tipologiaAttivitaDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero TipologiaAttivita {}", e.getMessage(), e);

                GenericResponseDTO<List<TipologiaAttivitaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<AmbitoCompetenzaDTO>>> getAmbitoCompetenza()
    {
        log.info("Richiesta recupero AmbitoCompetenza");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/sezione332/ambito-competenza", webServiceType, headers,
                new ParameterizedTypeReference<List<AmbitoCompetenzaDTO>>() {})
            .doOnNext(response ->
                log.info("AmbitoCompetenza recuperati: {} elementi", response != null ? response.size() : 0))
            .map(ambitoCompetenzaDTOList -> {
                GenericResponseDTO<List<AmbitoCompetenzaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(ambitoCompetenzaDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero AmbitoCompetenza {}", e.getMessage(), e);

                GenericResponseDTO<List<AmbitoCompetenzaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<AreaTematicaDTO>>> getAreaTematica()
    {
        log.info("Richiesta recupero AreaTematica");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/sezione332/area-tematica", webServiceType, headers,
                new ParameterizedTypeReference<List<AreaTematicaDTO>>() {})
            .doOnNext(response ->
                log.info("AreaTematica recuperati: {} elementi", response != null ? response.size() : 0))
            .map(areaTematicaDTOList -> {
                GenericResponseDTO<List<AreaTematicaDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(areaTematicaDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero AreaTematica {}", e.getMessage(), e);

                GenericResponseDTO<List<AreaTematicaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<TipologiaDestinatariDTO>>> getTipologiaDestinatari()
    {
        log.info("Richiesta recupero TipologiaDestinatari");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/sezione332/tipologia-destinatari", webServiceType, headers,
                new ParameterizedTypeReference<List<TipologiaDestinatariDTO>>() {})
            .doOnNext(response ->
                log.info("TipologiaDestinatari recuperati: {} elementi", response != null ? response.size() : 0))
            .map(tipologiaDestinatariDTOList -> {
                GenericResponseDTO<List<TipologiaDestinatariDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(tipologiaDestinatariDTOList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero TipologiaDestinatari {}", e.getMessage(), e);

                GenericResponseDTO<List<TipologiaDestinatariDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }
}
