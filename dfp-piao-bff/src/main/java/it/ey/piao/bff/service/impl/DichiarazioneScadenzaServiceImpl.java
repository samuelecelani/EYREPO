package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.StatoDichiarazioneEnum;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IDichiarazioneScadenzaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
public class DichiarazioneScadenzaServiceImpl implements IDichiarazioneScadenzaService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(DichiarazioneScadenzaServiceImpl.class);

    public DichiarazioneScadenzaServiceImpl(WebClientService webClientService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> saveOrUpdate(DichiarazioneScadenzaDTO request)
    {
        log.info("Richiesta salvataggio/modifica DichiarazioneScadenza");

        HttpHeaders headers = new HttpHeaders();
        // Risposta in formato JSON
        headers.set("Accept", "application/json");

        // chiamata post verso l'endpoint del BE
        return webClientService.post("/dichiarazione-scadenza", webServiceType, request, headers, DichiarazioneScadenzaDTO.class)

            // logghiamo la risposta ricevuta
            .doOnNext(response ->
                log.info("DichiarazioneScadenza salvato/modificato: {}", response))

            // mappiamo la risposta
            .map(dichiarazione -> {GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();

                // se ci sono errori
                if (dichiarazione == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError()
                        .setMessageError("Errore nel salvataggio/modifica DichiarazioneScadenza");
                }
                // inseriamo i dati
                finalResponse.setData(dichiarazione);

                // crea e valorizza lo status
                finalResponse.setStatus(new Status());

                // se tutto regolare indichiamo che l'operazione è andata a buon fine
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            // gestiamo gli errori HTTP con log dell'errore
            .onErrorResume(e -> {
                log.error("Errore salvataggio/modifica DichiarazioneScadenza: {}",
                    e.getMessage(), e);
                // costruiamo la risposta dell'errore  ,status fallito
                GenericResponseDTO<DichiarazioneScadenzaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                // restituiamo un Mono con la risposta dell'errore
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<Void> deleteById(Long id)
    {
        // log della richiesta

        log.info("Richiesta cancellazione DichiarazioneScadenza con id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        //chiamata delete
        return webClientService.delete("/dichiarazione-scadenza/" + id, webServiceType, headers, Void.class)
            .doOnSuccess(response -> log.info("DichiarazioneScadenza con id={} cancellata con successo", id))
            .then()
            .onErrorResume(e -> {
                log.error("Errore cancellazione DichiarazioneScadenza con id={}: {}", id, e.getMessage(), e);
                return Mono.error(e);
            });
    }

    @Override
    public Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> getExistingDichiarazioneScadenza(String codPAFK) {
        log.info("Richiesta recupero DichiarazioneScadenzaDTO per codPAFK", codPAFK);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/dichiarazione-scadenza/" + codPAFK, webServiceType, headers, DichiarazioneScadenzaDTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(d -> {
                GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();
                if (d == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero DichiarazioneScadenzaDTO per codPAFK: " + codPAFK);
                }
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica DichiarazioneScadenzaDTO {}", e);
                GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> findByIdPiao(Long idPiao) {
        log.info("Richiesta recupero DichiarazioneScadenza per idPiao={}", idPiao);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/dichiarazione-scadenza/by-piao/" + idPiao, webServiceType, headers, DichiarazioneScadenzaDTO.class)
            .doOnNext(response -> log.info("DichiarazioneScadenza recuperata per idPiao={}: {}", idPiao, response))
            .map(d -> {
                GenericResponseDTO<DichiarazioneScadenzaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero DichiarazioneScadenza per idPiao={}: {}", idPiao, e.getMessage(), e);
                GenericResponseDTO<DichiarazioneScadenzaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<StoricoDichiarazioneDFPDTO>>> findAllStorico() {
        log.info("Richiesta recupero storico dichiarazioni DFP");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/dichiarazione-scadenza/all",
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<StoricoDichiarazioneDFPDTO>>() {})
            .doOnNext(response -> log.info("Storico dichiarazioni DFP recuperato: {} elementi",
                response != null ? response.size() : 0))
            .map(list -> {
                GenericResponseDTO<List<StoricoDichiarazioneDFPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(list != null ? list : Collections.emptyList());
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero storico dichiarazioni DFP: {}", e.getMessage(), e);
                GenericResponseDTO<List<StoricoDichiarazioneDFPDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> updateStato(Long id, Boolean stato) {
        log.info("Richiesta aggiornamento stato DichiarazioneScadenza id={} stato={}", id, stato);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.patch(
                "/dichiarazione-scadenza/" + id + "/stato",
                webServiceType,
                stato,
                headers,
                Void.class)
            .doOnNext(response -> log.info("Stato DichiarazioneScadenza aggiornato per id={}", id))
            .map(d -> buildOkVoid())
            .defaultIfEmpty(buildOkVoid())
            .onErrorResume(e -> {
                log.error("Errore aggiornamento stato DichiarazioneScadenza id={}: {}", id, e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    private static GenericResponseDTO<Void> buildOkVoid() {
        GenericResponseDTO<Void> r = new GenericResponseDTO<>();
        r.setStatus(new Status());
        r.getStatus().setSuccess(Boolean.TRUE);
        return r;
    }

    @Override
    public Mono<GenericResponseDTO<List<SollecitiDichiarazioniDFPDTO>>> searchDichiarazioni(String denominazionePiao,
                                                                                            String tipologiaIstat,
                                                                                            String codPAFK,
                                                                                            StatoDichiarazioneEnum statoDichiarazione) {
        log.info("Richiesta ricerca dichiarazioni: denominazione='{}', tipologia='{}', codPAFK='{}', stato='{}'",
            denominazionePiao, tipologiaIstat, codPAFK, statoDichiarazione);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // Costruzione querystring con soli filtri valorizzati
        StringBuilder url = new StringBuilder("/dichiarazione-scadenza/search?denominazionePiao=")
            .append(denominazionePiao != null ? denominazionePiao : "");
        if (tipologiaIstat != null && !tipologiaIstat.isBlank()) {
            url.append("&tipologiaIstat=").append(tipologiaIstat);
        }
        if (codPAFK != null && !codPAFK.isBlank()) {
            url.append("&codPAFK=").append(codPAFK);
        }
        if (statoDichiarazione != null) {
            url.append("&statoDichiarazione=").append(statoDichiarazione.name());
        }

        return webClientService.get(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<SollecitiDichiarazioniDFPDTO>>() {})
            .doOnNext(response -> log.info("Ricerca dichiarazioni: {} elementi",
                response != null ? response.size() : 0))
            .map(list -> {
                GenericResponseDTO<List<SollecitiDichiarazioniDFPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(list != null ? list : Collections.emptyList());
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore ricerca dichiarazioni: {}", e.getMessage(), e);
                GenericResponseDTO<List<SollecitiDichiarazioniDFPDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<PageDTO<SollecitiDichiarazioniDFPDTO>>> searchDichiarazioniPaged(String denominazionePiao,
                                                                                                    String tipologiaIstat,
                                                                                                    String codPAFK,
                                                                                                    StatoDichiarazioneEnum statoDichiarazione,
                                                                                                    int page,
                                                                                                    int size,
                                                                                                    List<String> sort) {
        log.info("Richiesta ricerca PAGED dichiarazioni: denominazione='{}', tipologia='{}', codPAFK='{}', stato='{}', page={}, size={}, sort={}",
            denominazionePiao, tipologiaIstat, codPAFK, statoDichiarazione, page, size, sort);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/dichiarazione-scadenza/search/paged?denominazionePiao=")
            .append(denominazionePiao != null ? denominazionePiao : "")
            .append("&page=").append(page)
            .append("&size=").append(size);
        if (tipologiaIstat != null && !tipologiaIstat.isBlank()) {
            url.append("&tipologiaIstat=").append(tipologiaIstat);
        }
        if (codPAFK != null && !codPAFK.isBlank()) {
            url.append("&codPAFK=").append(codPAFK);
        }
        if (statoDichiarazione != null) {
            url.append("&statoDichiarazione=").append(statoDichiarazione.name());
        }
        if (sort != null) {
            for (String s : sort) {
                if (s != null && !s.isBlank()) {
                    url.append("&sort=").append(s);
                }
            }
        }

        return webClientService.get(
                url.toString(),
                webServiceType,
                headers,
                new ParameterizedTypeReference<PageDTO<SollecitiDichiarazioniDFPDTO>>() {})
            .doOnNext(p -> log.info("Ricerca PAGED dichiarazioni: page={}/{} totalElements={}",
                p != null ? p.getNumber() : -1,
                p != null ? p.getTotalPages() : -1,
                p != null ? p.getTotalElements() : -1))
            .map(p -> {
                GenericResponseDTO<PageDTO<SollecitiDichiarazioniDFPDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(p);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore ricerca PAGED dichiarazioni: {}", e.getMessage(), e);
                GenericResponseDTO<PageDTO<SollecitiDichiarazioniDFPDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
}
