package it.ey.piao.bff.service.impl;

import it.ey.dto.ConfigurazioniDTO;
import it.ey.dto.Error;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Status;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IConfigurazioniService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class ConfigurazioniServiceImpl implements IConfigurazioniService
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;

    private static final Logger log = LoggerFactory.getLogger(ConfigurazioniServiceImpl.class);

    public ConfigurazioniServiceImpl(
        WebClientService webClientService
    )
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }

    @Override
    public Mono<GenericResponseDTO<List<ConfigurazioniDTO>>> getAllConfigurazioni()
    {
        log.info("Richiesta recupero ConfigurazioniDTO");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/configurazioni", webServiceType, headers, new ParameterizedTypeReference<List<ConfigurazioniDTO>>() {})
            .doOnNext(response -> log.info("Errore nel recupero Configurazione: {}", response))
            .map(d -> {
                GenericResponseDTO<List<ConfigurazioniDTO>> finalResponse = new GenericResponseDTO<>();
                if (d == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero ConfigurazioniDTO");
                }
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore recupero ConfigurazioniDTO {}", e.getMessage(), e);
                GenericResponseDTO<List<ConfigurazioniDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<String>>> getAllDataDaAndDataA()
    {
        log.info("Richiesta recupero DataDa e DataA di ConfigurazioniDTO");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/configurazioni/date", webServiceType, headers, new ParameterizedTypeReference<List<String>>() {})
            .doOnNext(response -> log.info("Errore nel recupero DataDa e DataA di Configurazione: {}", response))
            .map(d -> {
                GenericResponseDTO<List<String>> finalResponse = new GenericResponseDTO<>();
                if (d == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero DataDa e DataA di ConfigurazioniDTO");
                }
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore recupero DataDa e DataA di ConfigurazioniDTO {}", e.getMessage(), e);
                GenericResponseDTO<List<String>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> setValoreFromCodice(String codice, String valore)
    {
        log.info("Richiesta di aggiornamento valore configurazione con codice: {}", codice);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");

        return webClientService.put(
                "/configurazioni",
                webServiceType,
                ConfigurazioniDTO.builder().codice(codice).valore(valore).build(),
                headers,
                Void.class
            )
            .doOnNext(response -> log.info("Valore di Configurazione con codice {} aggioranto con successo", codice))
            .map(v -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nell'aggiornamento del valore di configurazione con codice {}: {}", codice, e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<String>> getValoreFromCodice(String codice)
    {
        log.info("Richiesta recupero valore da codice: {}", codice);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/configurazioni/value" + "?");
        if (codice != null && !codice.isBlank()) url.append("codice=").append(codice).append("&");

        return webClientService.get(url.toString(), webServiceType, headers, new ParameterizedTypeReference<String>() {})
            .doOnNext(response -> log.info("Errore nel recupero valore con codice: {}", codice))
            .map(d -> {
                GenericResponseDTO<String> finalResponse = new GenericResponseDTO<>();
                if (d == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero valore di ConfigurazioniDTO");
                }
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore recupero valore ConfigurazioniDTO {}", e.getMessage(), e);
                GenericResponseDTO<String> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<ConfigurazioniDTO>> getConfigurazioneByCodice(String codice)
    {
        log.info("Richiesta recupero Configurazione con codice={}", codice);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/configurazioni/codice/" + codice, webServiceType, headers, new ParameterizedTypeReference<ConfigurazioniDTO>() {})
            .map(d -> {
                GenericResponseDTO<ConfigurazioniDTO> finalResponse = new GenericResponseDTO<>();
                if (d == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Configurazione non trovata per codice=" + codice);
                }
                finalResponse.setData(d);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero Configurazione con codice={}: {}", codice, e.getMessage(), e);
                GenericResponseDTO<ConfigurazioniDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
                return Mono.just(finalResponse);
            });
    }

    @Override
    public Mono<Map<String, String>> getPiaoDatesFree()
    {
        log.info("Richiesta recupero date PIAO da endpoint FREE del BE");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/configurazioni/free/piao-dates",
                webServiceType,
                headers,
                new ParameterizedTypeReference<Map<String, String>>() {})
            .doOnNext(m -> log.info("Date PIAO recuperate da BE (free): {}", m))
            .doOnError(e -> log.error("Errore recupero date PIAO (free): {}", e.getMessage(), e));
    }
}
