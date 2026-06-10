package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.dto.external.IpaAmministrazioneExternalDTO;
import it.ey.dto.external.IpaAmministrazionePublicExternalDTO;
import it.ey.dto.external.PiaoExternalDTO;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.ey.enums.Tipologia;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.global.exception.CustomBusinessException;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IPiaoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PiaoServiceImpl implements IPiaoService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final WebServiceType webServiceTypePP;
    private static final Logger log = LoggerFactory.getLogger(PiaoServiceImpl.class);

    public PiaoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.webServiceTypePP = WebServiceType.SYNC_PP;
    }
    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> initializePiao(PiaoDTO piaoDTO) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // Il triennio di riferimento è direttamente nel DTO (campo triennioRiferimento)
        String url = "/piao/getOrCreate";

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (principal instanceof UserDTO userDTO && userDTO.getPaRiferimento() != null) {
                    userDTO.getPaRiferimento().stream()
                        .filter(pa -> pa.getCodePA() != null && pa.getCodePA().equals(piaoDTO.getCodPAFK()))
                        .findFirst()
                        .ifPresent(pa -> {
                            piaoDTO.setDenominazionePA(pa.getDenominazionePA());
                            log.info("DenominazionePA impostata da sessione: {}", pa.getDenominazionePA());
                        });
                }
                return webClientService.post(url, webServiceType, piaoDTO, headers, PiaoDTO.class);
            })
            .switchIfEmpty(
                webClientService.post(url, webServiceType, piaoDTO, headers, PiaoDTO.class)
            )
            .doOnNext(response -> log.info("Piao inizializzato: {}", response))
            .map(piao -> {
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                if (piao == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Funzionalità Redigi PIAO disabilitata. Creare una nuova versione del PIAO");
                }
                finalResponse.setData(piao);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore inizializzazione PIAO {}", e);
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
    @Override
    public Mono<GenericResponseDTO<Boolean>> redigiPiaoIsAllowed(String codPAFK){

            // Validazione input
            if (!StringUtils.isNotBlank(codPAFK)) {
                log.error("CodPAFK mancante nel PiaoDTO");
                throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            return webClientService.get("/piao/redigiPiaoIsAllowed?codPAFK=" + codPAFK  , webServiceType,headers,Boolean.class)
                .doOnNext(response -> log.info("isRedigiPiaoAllowed: {}", response))
                .map(res ->{
                    GenericResponseDTO<Boolean> respose = new GenericResponseDTO<>();
                    respose.setData(res);
                    respose.setStatus(Status.builder().isSuccess(true).build());
                    return respose;
                });

    }

    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> getTipologiaCorrente(String codPAFK) {
        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("CodPAFK mancante");
            throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/piao/tipologia-corrente?codPAFK=" + codPAFK, webServiceType, headers, PiaoDTO.class)
            .doOnNext(response -> log.info("getTipologiaCorrente: {}", response))
            .map(res -> {
                GenericResponseDTO<PiaoDTO> response = new GenericResponseDTO<>();
                response.setData(res);
                response.setStatus(Status.builder().isSuccess(true).build());
                return response;
            });
    }

    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> findById(Long id) {
        log.info("Ricerca PIAO per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/piao/findById?id=" + id, webServiceType, headers, PiaoDTO.class)
            .doOnNext(response -> log.info("PIAO recuperato per id={}: {}", id, response))
            .map(piao -> {
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(piao);
                finalResponse.setStatus(Status.builder().isSuccess(true).build());
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero PIAO per id={}: {}", id, e.getMessage());
                GenericResponseDTO<PiaoDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(Status.builder().isSuccess(false).build());
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoDTO>>> findByCodPaFkAndIsCurrent(String codPAFK, boolean isCurrent) {
        // Validazione input
        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("CodPAFK mancante nel PiaoDTO");
            throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/piao/findByCodPaFkAndIsCurrent?codPAFK=" + codPAFK + "&isCurrent=" + isCurrent;
        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<PiaoDTO>>(){})
            .doOnNext(response -> log.info("findByCodPaFkAndIsCurrent: {}", response))
            .map(res ->{
                GenericResponseDTO<List<PiaoDTO>> respose = new GenericResponseDTO<>();
                respose.setData(res);
                respose.setStatus(Status.builder().isSuccess(true).build());
                return respose;
            });
    }

    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> findPiaoPrecedente(String codPAFK) {
        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("CodPAFK mancante");
            throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio");
        }
        log.info("Ricerca PIAO anno precedente per PA={}", codPAFK);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/piao/precedente?codPAFK=" + codPAFK, webServiceType, headers, PiaoDTO.class)
            .doOnNext(response -> log.info("PIAO precedente recuperato: {}", response))
            .map(piao -> {
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(piao);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero PIAO precedente per PA={}: {}", codPAFK, e.getMessage());
                GenericResponseDTO<PiaoDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> pubblicaPiao(ApprovazioneDTO approvazione)
    {
        log.info("Richiesta pubblicazione Piao");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.put("/piao/approvazione", webServiceType, approvazione, headers, Void.class)
            .doOnSuccess(response -> log.info("Approvazione salvata: {}", approvazione))
            .then(notifySyncPp(approvazione.getIdPiao()))
            .then(Mono.fromCallable(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            }))
            .doOnError(e -> log.error("Errore nella pubblicazione del Piao: {}", e.getMessage(), e));
    }


    @Override
    public Mono<GenericResponseDTO<ApprovazioneDTO>> getApprovazione(Long idPiao)
    {
        log.info("Richiesta recupero ApprovazioneDTO per idPiao={}", idPiao);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/piao/approvazione/" + idPiao, webServiceType, headers,
                new ParameterizedTypeReference<ApprovazioneDTO>() {})
            .doOnNext(response ->
                log.info("ApprovazioneDTO recuperata: {}", response ))
            .map(approvazione -> {
                GenericResponseDTO<ApprovazioneDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(approvazione);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);

                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero Approvazione per idPiao={}: {}", idPiao, e.getMessage(), e);

                GenericResponseDTO<ApprovazioneDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());

                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoDTO>>> consultazionePiao(String codPAFK, String denominazione, String versione, Tipologia tipologia) {

        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("codPAFK mancante");
            throw new CustomBusinessException("codPAFK è obbligatorio");
        }
        if (!StringUtils.isNotBlank(denominazione)) {
            log.error("denominazione mancante");
            throw new CustomBusinessException("denominazione è obbligatoria");
        }

        log.info("Consultazione PIAO per codPAFK={}, denominazione={}, versione={}, tipologia={}",
            codPAFK, denominazione, versione, tipologia);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/piao/findByDenominazioneVersione?codPAFK=" + codPAFK +
                    "&denominazione=" + denominazione +
                    (StringUtils.isNotBlank(versione) ? "&versione=" + versione : "") +
                    (tipologia != null ? "&tipologia=" + tipologia.name() : ""),
                webServiceType,
                headers,
                new ParameterizedTypeReference<List<PiaoDTO>>() {}
            )
            .doOnNext(response ->
                log.info("PIAO consultato: {}", response)
            )
            .map(lista -> {
                GenericResponseDTO<List<PiaoDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(lista);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore consultazione PIAO per codPAFK={}: {}", codPAFK, e.getMessage());
                GenericResponseDTO<List<PiaoDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> findPiaoLastVersion(
        String codPAFK,
        String denominazione
    ) {

        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("codPAFK mancante");
            throw new CustomBusinessException("codPAFK è obbligatorio");
        }
        if (!StringUtils.isNotBlank(denominazione)) {
            log.error("denominazione mancante");
            throw new CustomBusinessException("denominazione è obbligatoria");
        }

        log.info("Consultazione ultima versione PIAO per codPAFK={}, denominazione={}",
            codPAFK, denominazione);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/piao/ultima-versione?codPAFK=" + codPAFK +
                    "&denominazione=" + denominazione,
                webServiceType,
                headers,
                PiaoDTO.class
            )
            .doOnNext(response ->
                log.info("Ultima versione PIAO consultata: {}", response)
            )
            .map(piao -> {
                GenericResponseDTO<PiaoDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(piao);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore consultazione ultima versione PIAO per codPAFK={}: {}",
                    codPAFK, e.getMessage());
                GenericResponseDTO<PiaoDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long idPiao) {
        log.info("Richiesta validazione stato PIAO per id={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/piao/validazione/" + idPiao, webServiceType, new PiaoDTO(), headers, Void.class)
            .doOnNext(response -> log.info("Modifica Stato PIAO: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore richiesta validazione PIAO {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long idPiao) {
        log.info("Validazione PIAO per id={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/piao/valida-sezione/" + idPiao, webServiceType, new PiaoDTO(), headers, Void.class)
            .doOnNext(response -> log.info("Valida PIAO: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore valida PIAO {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> rifiutaValidazione(Long idPiao, String osservazioni) {
        log.info("Rifiuto validazione PIAO per id={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/piao/rifiuta-validazione/" + idPiao, webServiceType, osservazioni, headers, Void.class)
            .doOnNext(response -> log.info("Rifiuta validazione PIAO: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione PIAO {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> revocaValidazione(Long idPiao, String osservazioni) {
        log.info("Revoca validazione PIAO per id={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/piao/revoca-validazione/" + idPiao, webServiceType, osservazioni, headers, Void.class)
            .doOnNext(response -> log.info("Revoca validazione PIAO: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore revoca validazione PIAO {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> annullaValidazione(Long idPiao) {
        log.info("Annulla validazione PIAO per id={}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/piao/annulla-validazione/" + idPiao, webServiceType, new PiaoDTO(), headers, Void.class)
            .doOnNext(response -> log.info("Annulla validazione PIAO: {}", response))
            .map(response -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore annulla validazione PIAO {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<it.ey.dto.external.PiaoExternalDTO>> findPiaoExternal(String codPAFK) {
        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("codPAFK mancante");
            throw new CustomBusinessException("codPAFK è obbligatorio");
        }

        log.info("Ricerca PIAO External per codPAFK={}", codPAFK);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String url = "/piao/external?codPAFK=" + codPAFK ;

        return webClientService.get(url, webServiceType, headers, PiaoExternalDTO.class)
            .doOnNext(response -> log.info("PIAO External recuperato: {}", response))
            .map(piaoExternal -> {
                GenericResponseDTO<PiaoExternalDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(piaoExternal);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero PIAO External per codPAFK={}: {}", codPAFK, e.getMessage());
                GenericResponseDTO<PiaoExternalDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoExternalDTO>>> findPiaoExternalByIds(List<Long> idPiaoList) {
        if (idPiaoList == null || idPiaoList.isEmpty()) {
            log.error("Lista idPiao mancante o vuota");
            throw new CustomBusinessException("La lista di idPiao è obbligatoria");
        }

        log.info("Ricerca PIAO External per idPiaoList={}", idPiaoList);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");

        return webClientService.post("/piao/external/byIds", webServiceType, idPiaoList, headers,
                new ParameterizedTypeReference<List<PiaoExternalDTO>>() {})
            .doOnNext(response -> log.info("PIAO External recuperati per ids: {} risultati", response.size()))
            .map(piaoExternalList -> {
                GenericResponseDTO<List<PiaoExternalDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(piaoExternalList);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero PIAO External per idPiaoList={}: {}", idPiaoList, e.getMessage());
                GenericResponseDTO<List<PiaoExternalDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<String>>> getTrienniRiferimento() {
        log.info("Recupero trienni di riferimento");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get("/piao/trienni-riferimento", webServiceType, headers, String[].class)
            .doOnNext(response -> log.info("Trienni di riferimento recuperati: {}", Arrays.toString(response)))
            .map(response -> {
                GenericResponseDTO<List<String>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                finalResponse.setData(Arrays.asList(response));
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero trienni di riferimento {}", e.getMessage(), e);
                GenericResponseDTO<List<String>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoDTO>>> findAllPiaoPubblicatiByCodePA(String codPAFK) {
        log.info("Recupero PIAO pubblicati con codPAFK={}", codPAFK);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        String url = StringUtils.isNotBlank(codPAFK)
            ? "/piao/pubblicati?codPAFK=" + codPAFK
            : "/piao/pubblicati";

        return webClientService.get(url, webServiceType, headers, new ParameterizedTypeReference<List<PiaoDTO>>() {})
            .doOnNext(response -> log.info("PIAO pubblicati recuperati: {}", response))
            .map(lista -> {
                GenericResponseDTO<List<PiaoDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(lista);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero PIAO pubblicati per codPAFK={}: {}", codPAFK, e.getMessage());
                GenericResponseDTO<List<PiaoDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoDTO>>> searchPubblicati(String codiceIpa, String tipologia) {
        if (!StringUtils.isNotBlank(codiceIpa) && !StringUtils.isNotBlank(tipologia)) {
            log.error("Nessun filtro specificato per la ricerca PIAO pubblicati");
            throw new CustomBusinessException("È necessario specificare almeno un filtro tra codiceIpa e tipologia");
        }

        log.info("Ricerca PIAO pubblicati con codiceIpa={}, tipologia={}", codiceIpa, tipologia);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/piao/pubblicati/search");
        List<String> params = new java.util.ArrayList<>();

        if (StringUtils.isNotBlank(codiceIpa)) {
            params.add("codiceIpa=" + codiceIpa);
        }
        if (StringUtils.isNotBlank(tipologia)) {
            params.add("tipologia=" + tipologia);
        }

        if (!params.isEmpty()) {
            url.append("?").append(String.join("&", params));
        }

        return webClientService.get(url.toString(), webServiceType, headers, new ParameterizedTypeReference<List<PiaoDTO>>() {})
            .doOnNext(response -> log.info("PIAO pubblicati trovati: {} elementi", response != null ? response.size() : 0))
            .map(lista -> {
                GenericResponseDTO<List<PiaoDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(lista);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore ricerca PIAO pubblicati: {}", e.getMessage(), e);
                GenericResponseDTO<List<PiaoDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<PiaoDTO>>> searchPubblicatiByDenominazione(String denominazione, String tipologia) {
        if (!StringUtils.isNotBlank(denominazione)) {
            log.error("denominazione mancante");
            throw new CustomBusinessException("denominazione è obbligatoria");
        }

        log.info("Ricerca PIAO pubblicati con denominazione={}, tipologia={}", denominazione, tipologia);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        StringBuilder url = new StringBuilder("/piao/pubblicati/search-by-denominazione?denominazione=" + denominazione);

        if (StringUtils.isNotBlank(tipologia)) {
            url.append("&tipologia=").append(tipologia);
        }

        return webClientService.get(url.toString(), webServiceType, headers, new ParameterizedTypeReference<List<PiaoDTO>>() {})
            .doOnNext(response -> log.info("PIAO pubblicati trovati per denominazione: {} elementi", response != null ? response.size() : 0))
            .map(lista -> {
                GenericResponseDTO<List<PiaoDTO>> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(lista);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore ricerca PIAO pubblicati per denominazione: {}", e.getMessage(), e);
                GenericResponseDTO<List<PiaoDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> salvaInBozzaPiaoPDF(PiaoDTO piao) {
        log.info("Richiesta salvataggio in bozza Piao PDF");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.put("/piao/salva-bozza-pdf", webServiceType, piao, headers, Void.class)
            .doOnNext(response -> log.info("Piao PDF salvato in bozza: {}", piao))
            .flatMap(f -> saveAnagraficaForPiao(piao.getId())
                .thenReturn(f))
            .map(f -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel salvataggio del Piao PDF {}", e);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> pubblicaPiaoPDF(PiaoDTO piao) {
        log.info("Richiesta pubblicazione Piao PDF");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.put("/piao/pubblica-pdf", webServiceType, piao, headers, Void.class)
            .doOnSuccess(response -> log.info("Piao PDF Pubblicato : {}", piao))
            .then(saveAnagraficaForPiao(piao.getId()))
            .then(notifySyncPp(piao.getId()))
            .then(Mono.fromCallable(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            }))
            .doOnError(e -> log.error("Errore nella pubblicazione del Piao PDF: {}", e.getMessage(), e));
    }

    /**
     * Notifica al modulo dfp-sync-pp l'avvenuta pubblicazione del PIAO.
     * Best-effort: eventuali errori vengono loggati ma NON interrompono il flusso principale.
     */
    private Mono<Void> notifySyncPp(Long idPiao) {
        if (idPiao == null) {
            log.warn("[sync-pp] idPiao null: skip notifica sync");
            return Mono.empty();
        }

        HttpHeaders syncHeaders = new HttpHeaders();
        syncHeaders.set("Accept", "application/json");
        log.info("[sync-pp] Invio sync per idPiao={} (baseUrl={})", idPiao, webServiceTypePP.getUrl());
        return webClientService.get("/api/v1/sync/piao?idPiao=" + idPiao, webServiceTypePP, syncHeaders, Void.class)
            .doOnSuccess(v -> log.info("[sync-pp] Sync completato per idPiao={}", idPiao))
            .onErrorResume(e -> {
                log.error("[sync-pp] Errore notifica sync per idPiao={}: {}", idPiao, e.getMessage(), e);
                return Mono.empty();
            })
            .then();
    }

    /**
     * Recupera l'anagrafica da IPA (usando il CF dell'utente in sessione) e la salva sul BE
     * associandola al Piao indicato. Se il CF non è disponibile, non effettua il salvataggio.
     */
    private Mono<Void> saveAnagraficaForPiao(Long idPiao) {
        if (idPiao == null) {
            log.warn("saveAnagraficaForPiao: idPiao è null, skip salvataggio anagrafica");
            return Mono.empty();
        }
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(ctx -> {
                var auth = ctx.getAuthentication();
                if (auth == null || !(auth.getPrincipal() instanceof UserDTO user)) {
                    log.warn("saveAnagraficaForPiao: impossibile recuperare utente dalla sessione");
                    return Mono.empty();
                }
                // Recupera il codice fiscale dalla PA attiva
                String codiceFiscale = null;
                if (user.getPaRiferimento() != null) {
                    codiceFiscale = user.getPaRiferimento().stream()
                        .filter(PaRiferimentoDTO::isAttiva)
                        .map(PaRiferimentoDTO::getFiscalCode)
                        .filter(cf -> cf != null && !cf.isBlank())
                        .findFirst()
                        .orElse(null);
                }
                if (codiceFiscale == null || codiceFiscale.isBlank()) {
                    log.warn("saveAnagraficaForPiao: codiceFiscale non disponibile, skip salvataggio anagrafica");
                    return Mono.empty();
                }
                final String cf = codiceFiscale;
                log.info("saveAnagraficaForPiao: recupero anagrafica IPA per CF={} e idPiao={}", cf, idPiao);

                return getAnagraficaFromIpa(cf)
                    .flatMap(anagrafica -> {
                        anagrafica.setIdPiao(idPiao);

                        HttpHeaders h = new HttpHeaders();
                        h.set("Accept", "application/json");
                        h.setContentType(MediaType.APPLICATION_JSON);
                        return webClientService.post("/anagrafica/save", webServiceType, anagrafica, h, AnagraficaDTO.class)
                            .doOnNext(saved -> log.info("Anagrafica salvata per idPiao={}: id={}", idPiao, saved.getId()))
                            .then();
                    })
                    .onErrorResume(e -> {
                        log.error("Errore salvataggio anagrafica per idPiao={}: {}", idPiao, e.getMessage(), e);
                        return Mono.empty();
                    });
            })
            .then();
    }

    /**
     * Recupera l'anagrafica da IPA per il codice fiscale specificato.
     * Chiama DFP (GET) e DFP2 (POST) in parallelo e mappa il risultato in AnagraficaDTO.
     */
    private Mono<AnagraficaDTO> getAnagraficaFromIpa(String codiceFiscale) {
        log.info("Recupero anagrafica da IPA per codice fiscale: {}", codiceFiscale);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1) GET /api/Public/GetAdministrationsInfoForCodiceFiscale?filter={cf} (DFP)
        Mono<IpaAmministrazioneExternalDTO> getCall = webClientService.get(
                "/api/Public/GetAdministrationsInfoForCodiceFiscale?filter=" + codiceFiscale,
                WebServiceType.IPA, headers,
                IpaAmministrazioneExternalDTO.class, false)
            .doOnNext(r -> log.info("Risposta IPA GET ricevuta: {}", r))
            .doOnError(e -> log.error("Errore chiamata IPA GET: {}", e.getMessage(), e));

        // 2) POST /api/Public (DFP2) - body con il codice fiscale
        Map<String, Object> body = new HashMap<>();
        body.put("fiscalCode", codiceFiscale);

        Mono<List<IpaAmministrazionePublicExternalDTO>> postCall = webClientService.post(
                "/api/Public", WebServiceType.IPA, body, headers,
                new ParameterizedTypeReference<List<IpaAmministrazionePublicExternalDTO>>() {}, false)
            .doOnNext(r -> log.info("Risposta IPA POST ricevuta - {} elementi", r != null ? r.size() : 0))
            .doOnError(e -> log.error("Errore chiamata IPA POST: {}", e.getMessage(), e));

        return Mono.zip(getCall, postCall)
            .map(tuple -> mapToAnagrafica(codiceFiscale, tuple.getT1(), tuple.getT2()))
            .onErrorResume(e -> {
                log.error("Errore recupero anagrafica IPA per CF {}: {}", codiceFiscale, e.getMessage(), e);
                AnagraficaDTO fallback = new AnagraficaDTO();
                fallback.setCodiceFiscale(codiceFiscale);
                return Mono.just(fallback);
            });
    }

    /**
     * Mappatura dati IPA -> AnagraficaDTO.
     */
    private AnagraficaDTO mapToAnagrafica(String codiceFiscale,
                                          IpaAmministrazioneExternalDTO dfpResponse,
                                          List<IpaAmministrazionePublicExternalDTO> dfp2Response) {
        AnagraficaDTO anagrafica = new AnagraficaDTO();
        anagrafica.setCodiceFiscale(codiceFiscale);

        // ---- DFP (GET) ----
        if (dfpResponse != null) {
            anagrafica.setDenominazioneEnte(dfpResponse.getTipologiaIstat());
            anagrafica.setAcronimoPA(dfpResponse.getAcronimo());
            anagrafica.setTipologiaPA(dfpResponse.getTipologiaAmministrazione());
            anagrafica.setTipologiaIstat(dfpResponse.getTipologiaIstat());
            anagrafica.setWww(dfpResponse.getSitoIstituzionale());
            anagrafica.setPec(dfpResponse.getAltro());
            anagrafica.setSocial(dfpResponse.getFacebook());

            if (dfpResponse.getAreeOrganizzativeOmogenee() != null
                && !dfpResponse.getAreeOrganizzativeOmogenee().isEmpty()) {
                var aoo = dfpResponse.getAreeOrganizzativeOmogenee().get(0);
                anagrafica.setCodiceIPA(aoo.getCodiceAmministrazione());

                if (aoo.getUnitaOrganizzative() != null) {
                    aoo.getUnitaOrganizzative().stream()
                        .filter(uo -> "Ufficio_Transizione_Digitale".equalsIgnoreCase(uo.getCodiceUo()))
                        .findFirst()
                        .ifPresent(uo -> {
                            String nome = uo.getNomeResponsabile() != null ? uo.getNomeResponsabile() : "";
                            String cognome = uo.getCognomeResponsabile() != null ? uo.getCognomeResponsabile() : "";
                            anagrafica.setNomeRTD((nome + " " + cognome).trim());
                            anagrafica.setStrutturaRifRTD(uo.getDescrizioneUo());
                        });
                }
            }
        }

        // ---- DFP2 (POST) ----
        if (dfp2Response != null && !dfp2Response.isEmpty()) {
            IpaAmministrazionePublicExternalDTO amm = dfp2Response.get(0);
            anagrafica.setDenominazioneEnte(amm.getName());
            anagrafica.setMail(amm.getEmail());

            StringBuilder indirizzo = new StringBuilder();
            if (amm.getAddress() != null && !amm.getAddress().isBlank()) indirizzo.append(amm.getAddress());
            if (amm.getCity() != null && !amm.getCity().isBlank()) {
                if (indirizzo.length() > 0) indirizzo.append(", ");
                indirizzo.append(amm.getCity());
            }
            if (amm.getDistrict() != null && !amm.getDistrict().isBlank()) {
                indirizzo.append(" (").append(amm.getDistrict()).append(")");
            }
            if (indirizzo.length() > 0) anagrafica.setIndirizzoSedeLegale(indirizzo.toString());

            if (amm.getNotStructuredData() != null) {
                amm.getNotStructuredData().stream()
                    .filter(n -> "BDAP".equalsIgnoreCase(n.getSource()) && n.getNotStructuredData() != null)
                    .findFirst()
                    .ifPresent(n -> {
                        Object piva = n.getNotStructuredData().get("PartitaIva");
                        if (piva != null) anagrafica.setPiva(piva.toString());
                    });
            }
        }

        return anagrafica;
    }
}
