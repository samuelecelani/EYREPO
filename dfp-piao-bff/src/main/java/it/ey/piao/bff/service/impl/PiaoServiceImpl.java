package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.dto.external.PiaoExternalDTO;

import java.util.Arrays;
import java.util.List;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.global.exception.CustomBusinessException;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IPiaoService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class PiaoServiceImpl implements IPiaoService {
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private static final Logger log = LoggerFactory.getLogger(PiaoServiceImpl.class);

    public PiaoServiceImpl(WebClientService webClientService) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
    }
    @Override
    public Mono<GenericResponseDTO<PiaoDTO>> initializePiao(PiaoDTO piaoDTO, String triennioRiferimento) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        // Imposta il triennio di riferimento nel DTO se fornito come parametro
        if (StringUtils.isNotBlank(triennioRiferimento)) {
            piaoDTO.setTriennioRiferimento(triennioRiferimento);
        }

        String url = "/piao/getOrCreate" + (StringUtils.isNotBlank(triennioRiferimento)
            ? "?triennioRiferimento=" + triennioRiferimento : "");

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
    public Mono<GenericResponseDTO<List<PiaoDTO>>> findPiaoByCodPAFK(String codPAFK) {
        // Validazione input
        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("CodPAFK mancante nel PiaoDTO");
            throw new CustomBusinessException("Il codice della pubblica amministrazione è obbligatorio per creare o recuperare il PIAO");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/piao/findBycodPAFK?codPAFK=" + codPAFK , webServiceType,headers,new ParameterizedTypeReference<List<PiaoDTO>>(){})
            .doOnNext(response -> log.info("findPiaosByCodPAFK: {}", response))
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
            .doOnNext(response -> log.info("Approvazione salvata: {}", approvazione))
            .map(f -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nel recupero dei dati {}", e);
            });
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
    public Mono<GenericResponseDTO<List<PiaoDTO>>> consultazionePiao(String codPAFK, String denominazione, String versione) {

        if (!StringUtils.isNotBlank(codPAFK)) {
            log.error("codPAFK mancante");
            throw new CustomBusinessException("codPAFK è obbligatorio");
        }
        if (!StringUtils.isNotBlank(denominazione)) {
            log.error("denominazione mancante");
            throw new CustomBusinessException("denominazione è obbligatoria");
        }

        log.info("Consultazione PIAO per codPAFK={}, denominazione={}, versione={}",
            codPAFK, denominazione, versione);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService.get(
                "/piao/findByDenominazioneVersione?codPAFK=" + codPAFK +
                    "&denominazione=" + denominazione +
                    (StringUtils.isNotBlank(versione) ? "&versione=" + versione : ""),
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
            .doOnNext(response -> log.info("Piao PDF Pubblicato : {}", piao))
            .map(f -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore nella pubblicazione del Piao PDF {}", e);
            });
    }
}
