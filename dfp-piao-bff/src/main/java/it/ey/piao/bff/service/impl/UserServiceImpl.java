package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.Tipologia;
import it.ey.enums.TypeAuthority;
import it.ey.enums.WebServiceType;
import it.ey.externaldto.ExternalApiResponse;
import it.ey.externaldto.ExternalPageResponse;
import it.ey.externaldto.RoleDto;
import it.ey.externaldto.UserProfileDto;
import it.ey.externaldto.mapper.MapperUtenti;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements IUserService {

    private final JwtClaimsService jwtClaimsService;
    private final WebClientService webClientService;
    private final List<WebServiceType> webServiceType;
    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final String ROLE_REFERENTE = "ROLE_REFERENTE";
    private final String ROLE_VALIDATORE = "ROLE_VALIDATORE";
    private final String ROLE_COORDINATORE_AMMINISTRATIVO = "ROLE_COORDINATORE_AMMINISTRATIVO";

    @Autowired
    public UserServiceImpl(JwtClaimsService jwtClaimsService, WebClientService webClientService) {
        this.jwtClaimsService = jwtClaimsService;
        this.webClientService = webClientService;
        this.webServiceType = List.of(WebServiceType.API, WebServiceType.BIP);
    }

    @Override
    public Mono<GenericResponseDTO<UserDTO>> getUserbyToken() {
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                GenericResponseDTO<UserDTO> response = new GenericResponseDTO<>();
                response.setStatus(new Status());

                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    response.getStatus().setSuccess(false);
                    response.setError(new Error());
                    response.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    return Mono.just(response);
                }

                if (StringUtils.isBlank(userDTO.getFiscalCode())) {
                    log.warn("Codice fiscale assente nel profilo utente");
                    response.getStatus().setSuccess(false);
                    response.setError(new Error());
                    response.getError().setMessageError("Invalid or null fiscal code");
                    return Mono.just(response);
                }

                log.info("Profilo utente recuperato da X-User-Profile per CF={}", userDTO.getFiscalCode());

                // Per ogni PA di riferimento, recupero le sezioni dal BE
                if (userDTO.getPaRiferimento() == null || userDTO.getPaRiferimento().isEmpty()) {
                    response.setData(userDTO);
                    response.getStatus().setSuccess(true);
                    return Mono.just(response);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());

                return reactor.core.publisher.Flux.fromIterable(userDTO.getPaRiferimento())
                    .flatMap(pa -> {
                        String ammId = pa.getExternalId();
                        if (StringUtils.isBlank(ammId) || StringUtils.isBlank(userDTO.getExternalId())) {
                            return Mono.just(pa);
                        }
                        return webClientService.get(
                            "/utente/" + userDTO.getExternalId() + "/sezioni?idAmministrazione=" + ammId,
                            webServiceType.getFirst(),
                            headers,
                            new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {}
                        ).map(sezioni -> {
                            if (sezioni != null && !sezioni.isEmpty()) {
                                // Popola sezioneAssociata per ogni ruolo della PA
                                if (pa.getRuoli() != null) {
                                    for (RuoloUserDTO ruolo : pa.getRuoli()) {
                                        List<String> sezioniPerRuolo = sezioni.stream()
                                            .filter(s -> ruolo.getCodice() != null && ruolo.getCodice().equals(s.getCodiceRuolo()))
                                            .map(s -> s.getStrutturaPiao() != null && s.getStrutturaPiao().getNumeroSezione() != null
                                                ? "SEZIONE_" + s.getStrutturaPiao().getNumeroSezione().replace(".", "")
                                                : null)
                                            .filter(Objects::nonNull)
                                            .distinct()
                                            .toList();
                                        ruolo.setSezioneAssociata(sezioniPerRuolo);
                                    }
                                }
                            }
                            return pa;
                        }).onErrorResume(err -> {
                            log.warn("Impossibile recuperare sezioni per PA {} utente {}: {}", ammId, userDTO.getExternalId(), err.getMessage());
                            return Mono.just(pa);
                        });
                    })
                    .collectList()
                    .map(paList -> {
                        userDTO.setPaRiferimento(paList);
                        response.setData(userDTO);
                        response.getStatus().setSuccess(true);
                        return response;
                    });
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.error("Nessun contesto di sicurezza trovato");
                GenericResponseDTO<UserDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(false);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError("Nessun contesto di sicurezza disponibile");
                return Mono.just(errorResponse);
            }));
    }

    @Override
    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByPa(String codicePa, List<String> roleNames) {
        log.info("Richiesta lista utenti per codicePa='{}', roleNames={}", codicePa, roleNames);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    errorResponse.setData(Collections.emptyList());
                    return Mono.just(errorResponse);
                }

                // Recupera amministrazioneId (externalId) dalla PA il cui codePA corrisponde al codicePa passato in input
                String amministrazioneId = userDTO.getPaRiferimento() != null
                    ? userDTO.getPaRiferimento().stream()
                      .filter(pa -> codicePa != null && codicePa.equals(pa.getCodePA()))
                      .map(PaRiferimentoDTO::getExternalId)
                      .findFirst()
                      .orElse(null)
                    : null;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());

                if (amministrazioneId == null) {
                    log.warn("Nessun amministrazioneId trovato per codicePa='{}', impossibile chiamare BIP", codicePa);
                    GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Nessuna amministrazione trovata per codicePa=" + codicePa);
                    errorResponse.setData(Collections.emptyList());
                    return Mono.just(errorResponse);
                }

                StringBuilder urlBuilder = new StringBuilder("/utenti/public/amministrazione/list_users?amministrazioneId=").append(amministrazioneId);
                if (roleNames != null && !roleNames.isEmpty()) {
                    urlBuilder.append("&roleNames=").append(String.join(",", roleNames));
                }
                String url = urlBuilder.toString();

                return webClientService.get(
                        url,
                        webServiceType.get(1), // BIP
                        headers,
                        new ParameterizedTypeReference<ExternalApiResponse<ExternalPageResponse<UserProfileDto>>>() {
                        }
                    )
                    .doOnNext(response -> log.info("Risposta BIP ricevuta per codicePa={}", codicePa))
                    .flatMap(bipResponse -> {
                        GenericResponseDTO<List<UtenteRuoloPaDTO>> finalResponse = new GenericResponseDTO<>();
                        finalResponse.setStatus(new Status());
                        if (bipResponse == null || bipResponse.getData() == null || bipResponse.getData().getContent() == null) {
                            log.warn("Nessun dato ricevuto da BIP per codicePa='{}'", codicePa);
                            finalResponse.setData(Collections.emptyList());
                            finalResponse.getStatus().setSuccess(Boolean.TRUE);
                            return Mono.just(finalResponse);
                        }
                        List<UserProfileDto> bipUtenti = bipResponse.getData().getContent();
                        // Per ogni utente, recupero anche le sezioni associate dal BE
                        return reactor.core.publisher.Flux.fromIterable(bipUtenti)
                            .flatMap(userProfileDto -> {
                                UtenteRuoloPaDTO utente = MapperUtenti.convert(userProfileDto, codicePa);
                                // Recupero le sezioni dal BE tramite externalUserId
                                return webClientService.get(
                                    "/utente/" + utente.getId() + "/sezioni?idAmministrazione=" + amministrazioneId,
                                    webServiceType.getFirst(),
                                    headers,
                                    new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {}
                                ).map(sezioni -> {
                                    if (sezioni != null && !sezioni.isEmpty()) {
                                        utente.setSezioni(sezioni);
                                    }
                                    return utente;
                                }).onErrorResume(err -> {
                                    log.warn("Impossibile recuperare sezioni per utente {}: {}", utente.getId(), err.getMessage());
                                    return Mono.just(utente);
                                });
                            })
                            .collectList()
                            .map(utenti -> {
                                // Solo per utenti con typeAuthority PA: mantieni solo i ruoli BIP che hanno sezioni nel nostro DB
                                boolean isDpf = TypeAuthority.DFP.equals(userDTO.getTypeAuthority());
                                List<UtenteRuoloPaDTO> filterUtenti = new ArrayList<>();
                                for (UtenteRuoloPaDTO utente : utenti) {
                                    if (!isDpf && utente.getRuoli() != null) {
                                        Set<String> ruoliConSezioni = utente.getSezioni() != null
                                            ? utente.getSezioni().stream()
                                                .map(UtenteRuoliPaSezioneDTO::getCodiceRuolo)
                                                .filter(c -> c != null)
                                                .collect(Collectors.toSet())
                                            : Collections.emptySet();
                                        utente.setRuoli(utente.getRuoli().stream()
                                            .filter(ruolo -> ruoliConSezioni.contains(ruolo.getCodiceRuolo()))
                                            .toList());
                                    }
                                    if (utente.getRuoli() != null && !utente.getRuoli().isEmpty()) {
                                        filterUtenti.add(utente);
                                    }
                                }
                                finalResponse.setData(filterUtenti);
                                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                                return finalResponse;
                            });
                    });
            })
            .onErrorResume(e -> {
                if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wcre) {
                    log.error("findUtentiByPa: BIP returned {} | responseBody={} | requestHeaders={}",
                        wcre.getStatusCode(), wcre.getResponseBodyAsString(), wcre.getRequest() != null ? wcre.getRequest().getHeaders() : "N/A");
                }
                log.error("Errore nel recupero dei dati da BIP per codicePa={}: {}", codicePa, e.getMessage(), e);
                GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                errorResponse.setData(Collections.emptyList());
                return Mono.just(errorResponse);
            });
    }
    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> saveUtenteByPa(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        log.info("Salvataggio utente/ruolo PA");

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    return Mono.just(errorResponse);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());

                log.info("Chiamata reale a BIP /utenti/management/create_sync");
                UserProfileDto userProfileDto = MapperUtenti.convert(utenteRuoloPaDTO);

                // Setto l'id esterno dell'amministrazione dal profilo utente in sessione
                if (userProfileDto.getAmministrazioni() != null && userDTO.getPaRiferimento() != null) {
                    userProfileDto.getAmministrazioni().stream()
                        .filter(amm -> amm.getIpaCode() != null)
                        .forEach(amm -> userDTO.getPaRiferimento().stream()
                            .filter(pa -> amm.getIpaCode().equals(pa.getCodePA()))
                            .map(PaRiferimentoDTO::getExternalId)
                            .findFirst()
                            .ifPresent(amm::setId));
                }

                // Recupero l'id amministrazione per le chiamate al BE interno
                String amministrazioneIdForBe = userProfileDto.getAmministrazioni() != null
                    ? userProfileDto.getAmministrazioni().stream()
                        .map(it.ey.externaldto.UserProfileDto.AmministrazioneDto::getId)
                        .filter(id -> id != null && !id.isBlank())
                        .findFirst().orElse("")
                    : "";

                return webClientService.post("/utenti/management/user/create_sync",
                        webServiceType.get(1), // BIP
                        userProfileDto,
                        headers,
                        new ParameterizedTypeReference<ExternalApiResponse<String>>() {})
                    .flatMap(bipResponse -> {
                        String externalUserId = bipResponse.getData();
                        log.info("Utente creato su BIP con id={}", externalUserId);
                        utenteRuoloPaDTO.setId(externalUserId);

                        if (utenteRuoloPaDTO.getSezioni() != null && !utenteRuoloPaDTO.getSezioni().isEmpty()) {
                            // Espando ruoli × sezioni: per ogni ruolo, una riga per ogni sezione
                            List<UtenteRuoliPaSezioneDTO> expandedSezioni = expandSezioniPerRuolo(utenteRuoloPaDTO);

                            // Salvo le sezioni sul BE interno
                            return webClientService.post(
                                    "/utente/" + externalUserId + "/sezioni?idAmministrazione=" + amministrazioneIdForBe,
                                    webServiceType.getFirst(),
                                    expandedSezioni,
                                    headers,
                                    new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {}
                                )
                                .map(savedSezioni -> {
                                    log.info("Sezioni salvate con successo per utente id={}", externalUserId);
                                    utenteRuoloPaDTO.setSezioni(savedSezioni);
                                    GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                    resp.setData(utenteRuoloPaDTO);
                                    resp.setStatus(new Status());
                                    resp.getStatus().setSuccess(Boolean.TRUE);
                                    return resp;
                                })
                                .onErrorResume(e -> {
                                    log.error("Errore nel salvataggio sezioni: {}", e.getMessage(), e);
                                    GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                    resp.setData(utenteRuoloPaDTO);
                                    resp.setStatus(new Status());
                                    resp.getStatus().setSuccess(Boolean.FALSE);
                                    resp.setError(new Error());
                                    resp.getError().setMessageError("Utente salvato su BIP ma errore sezioni: " + e.getMessage());
                                    return Mono.just(resp);
                                });
                        }

                        // Nessuna sezione: restituisco successo
                        GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                        resp.setData(utenteRuoloPaDTO);
                        resp.setStatus(new Status());
                        resp.getStatus().setSuccess(Boolean.TRUE);
                        return Mono.just(resp);
                    })
                    .doOnSuccess(r -> log.info("saveUtenteByPa completato"))
                    .onErrorResume(e -> {
                        log.error("Errore nel salvataggio utente su BIP {}", e.getMessage(), e);
                        GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                        errorResponse.setStatus(new Status());
                        errorResponse.getStatus().setSuccess(Boolean.FALSE);
                        errorResponse.setError(new Error());
                        errorResponse.getError().setMessageError(e.getMessage());
                        return Mono.just(errorResponse);
                    });
            });
    }


    /**
     * Recupera l'id esterno dell'utente da BIP tramite codice fiscale.
     * Chiama GET /utenti/public/user/list?codiceFiscale={cf}
     */
    private Mono<String> retrieveExternalUserIdByCf(String codiceFiscale, HttpHeaders headers) {
        log.info("Recupero id utente da BIP per CF={}", codiceFiscale);
        // Non più utilizzato nel flusso di save (create_sync ritorna l'id direttamente)
        return Mono.empty();
    }

    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> updateUtenteByPa(String id, String codicePa, UtenteRuoloPaDTO utenteRuoloPaDTO) {
        log.info("Aggiornamento utente/ruolo PA con id={}, codicePa={}", id, codicePa);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    return Mono.just(errorResponse);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());

                utenteRuoloPaDTO.setId(id);
                UserProfileDto userProfileDto = MapperUtenti.convert(utenteRuoloPaDTO);

                // Setto l'id esterno dell'amministrazione dal profilo utente in sessione
                if (userProfileDto.getAmministrazioni() != null && userDTO.getPaRiferimento() != null) {
                    userProfileDto.getAmministrazioni().stream()
                        .filter(amm -> amm.getIpaCode() != null)
                        .forEach(amm -> userDTO.getPaRiferimento().stream()
                            .filter(pa -> amm.getIpaCode().equals(pa.getCodePA()))
                            .map(PaRiferimentoDTO::getExternalId)
                            .findFirst()
                            .ifPresent(amm::setId));
                }

                // Recupero l'id amministrazione per le chiamate al BE interno
                String ammIdForBe = userDTO.getPaRiferimento() != null
                    ? userDTO.getPaRiferimento().stream()
                      .filter(pa -> codicePa != null && codicePa.equals(pa.getCodePA()))
                      .map(PaRiferimentoDTO::getExternalId)
                      .findFirst().orElse("")
                    : "";

                log.info("Chiamata reale a BIP PUT /utenti/management/update/{}", id);
                return webClientService.put(
                        "/utenti/management/user/update_sync/" + id,
                        webServiceType.get(1), // BIP
                        userProfileDto,
                        headers,
                        Void.class)
                    .then(Mono.defer(() -> {
                        log.info("Utente aggiornato su BIP con id={}", id);

                        if (utenteRuoloPaDTO.getSezioni() != null && !utenteRuoloPaDTO.getSezioni().isEmpty()) {
                            // Espando ruoli × sezioni: per ogni ruolo, una riga per ogni sezione
                            List<UtenteRuoliPaSezioneDTO> expandedSezioni = expandSezioniPerRuolo(utenteRuoloPaDTO);

                            // Salvo le sezioni sul BE interno
                            return webClientService.post(
                                    "/utente/" + id + "/sezioni?idAmministrazione=" + ammIdForBe,
                                    webServiceType.getFirst(),
                                    expandedSezioni,
                                    headers,
                                    new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {}
                                )
                                .map(savedSezioni -> {
                                    log.info("Sezioni salvate con successo per utente id={}", id);
                                    utenteRuoloPaDTO.setSezioni(savedSezioni);
                                    GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                    resp.setData(utenteRuoloPaDTO);
                                    resp.setStatus(new Status());
                                    resp.getStatus().setSuccess(Boolean.TRUE);
                                    return resp;
                                })
                                .onErrorResume(e -> {
                                    log.error("Errore nel salvataggio sezioni: {}", e.getMessage(), e);
                                    GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                    resp.setData(utenteRuoloPaDTO);
                                    resp.setStatus(new Status());
                                    resp.getStatus().setSuccess(Boolean.FALSE);
                                    resp.setError(new Error());
                                    resp.getError().setMessageError("Utente aggiornato su BIP ma errore sezioni: " + e.getMessage());
                                    return Mono.just(resp);
                                });
                        }

                        // Nessuna sezione: restituisco successo
                        GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                        resp.setData(utenteRuoloPaDTO);
                        resp.setStatus(new Status());
                        resp.getStatus().setSuccess(Boolean.TRUE);
                        return Mono.just(resp);
                    }))
                    .doOnSuccess(r -> log.info("updateUtenteByPa completato per id={}", id))
                    .onErrorResume(e -> {
                        log.error("Errore nell'aggiornamento utente su BIP id={}: {}", id, e.getMessage(), e);
                        GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                        errorResponse.setStatus(new Status());
                        errorResponse.getStatus().setSuccess(Boolean.FALSE);
                        errorResponse.setError(new Error());
                        errorResponse.getError().setMessageError(e.getMessage());
                        return Mono.just(errorResponse);
                    });
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> deleteUtentePa(String id, String codicePa) {
        log.info("Eliminazione utente/ruolo PA con id {}, codicePa={}", id, codicePa);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    return Mono.just(errorResponse);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());

                // Recupero l'id amministrazione per le chiamate al BE interno
                String ammIdForBe = userDTO.getPaRiferimento() != null
                    ? userDTO.getPaRiferimento().stream()
                      .filter(pa -> codicePa != null && codicePa.equals(pa.getCodePA()))
                      .map(PaRiferimentoDTO::getExternalId)
                      .findFirst().orElse("")
                    : "";

                // 1. Elimina l'utente su BIP
                return webClientService.delete(
                        "/utenti/management/user/delete/" + id,
                        webServiceType.get(1), // BIP
                        headers)
                    .then(Mono.defer(() -> {
                        log.info("Utente eliminato su BIP con id={}", id);

                        // 2. Elimina le sezioni associate sul BE interno (se presenti)
                        return webClientService.delete(
                                "/utente/" + id + "/sezioni?idAmministrazione=" + ammIdForBe,
                                webServiceType.getFirst(),
                                headers)
                            .then(Mono.defer(() -> {
                                log.info("Sezioni eliminate su BE per utente id={}", id);
                                GenericResponseDTO<Void> resp = new GenericResponseDTO<>();
                                resp.setStatus(new Status());
                                resp.getStatus().setSuccess(Boolean.TRUE);
                                return Mono.just(resp);
                            }))
                            .onErrorResume(e -> {
                                log.warn("Nessuna sezione da eliminare o errore per utente {}: {}", id, e.getMessage());
                                GenericResponseDTO<Void> resp = new GenericResponseDTO<>();
                                resp.setStatus(new Status());
                                resp.getStatus().setSuccess(Boolean.TRUE);
                                return Mono.just(resp);
                            });
                    }))
                    .doOnSuccess(r -> log.info("deleteUtentePa completato per id={}", id))
                    .onErrorResume(e -> {
                        log.error("Errore nell'eliminazione utente {} - {}", id, e.getMessage(), e);
                        GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                        errorResponse.setStatus(new Status());
                        errorResponse.getStatus().setSuccess(Boolean.FALSE);
                        errorResponse.setError(new Error());
                        errorResponse.getError().setMessageError(e.getMessage());
                        return Mono.just(errorResponse);
                    });
            });
    }

    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> retrieveProfileById(String externalId, String codePa) {
        log.info("Richiesta profilo utente da BIP per externalId='{}', codePa='{}'", externalId, codePa);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    return Mono.just(errorResponse);
                }
                String ammIdForBe = userDTO.getPaRiferimento() != null
                    ? userDTO.getPaRiferimento().stream()
                    .filter(pa -> codePa != null && codePa.equals(pa.getCodePA()))
                    .map(PaRiferimentoDTO::getExternalId)
                    .findFirst().orElse("")
                    : "";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());
                headers.set("X-Amministrazione-Id", ammIdForBe);
                String url = "/utenti/management/user/retrieve_profile/" + externalId;

                return webClientService.get(
                        url,
                        webServiceType.get(1), // BIP
                        headers,
                        new ParameterizedTypeReference<ExternalApiResponse<UserProfileDto>>() {
                        }
                    )
                    .doOnNext(response -> log.info("Risposta BIP ricevuta per externalId='{}'", externalId))
                    .flatMap(bipResponse -> {
                        GenericResponseDTO<UtenteRuoloPaDTO> finalResponse = new GenericResponseDTO<>();
                        finalResponse.setStatus(new Status());

                        if (bipResponse == null || bipResponse.getData() == null) {
                            log.warn("Nessun dato ricevuto da BIP per externalId='{}'", externalId);
                            finalResponse.getStatus().setSuccess(Boolean.FALSE);
                            finalResponse.setError(new Error());
                            finalResponse.getError().setMessageError("Nessun profilo trovato per externalId=" + externalId);
                            return Mono.just(finalResponse);
                        }

                        UtenteRuoloPaDTO utente = MapperUtenti.convert(bipResponse.getData(), codePa);
                        log.info("Profilo utente recuperato da BIP per externalId='{}', codePa='{}'", externalId, codePa);

                        // Recupero l'id amministrazione per la chiamata al BE interno
                        String ammId = userDTO.getPaRiferimento() != null
                            ? userDTO.getPaRiferimento().stream()
                              .filter(pa -> codePa != null && codePa.equals(pa.getCodePA()))
                              .map(PaRiferimentoDTO::getExternalId)
                              .findFirst().orElse("")
                            : "";

                        // Recupero le sezioni dal BE tramite externalUserId
                        return webClientService.get(
                            "/utente/" + utente.getId() + "/sezioni?idAmministrazione=" + ammId,
                            webServiceType.getFirst(),
                            headers,
                            new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {}
                        ).map(sezioni -> {
                            if (sezioni != null && !sezioni.isEmpty()) {
                                utente.setSezioni(sezioni);
                            }
                            // Filtro ruoli BIP: solo per utenti PA, mantieni solo quelli con sezioni nel nostro DB
                            if (utente.getRuoli() != null && !TypeAuthority.DFP.equals(userDTO.getTypeAuthority())) {
                                Set<String> ruoliConSezioni = utente.getSezioni() != null
                                    ? utente.getSezioni().stream()
                                        .map(UtenteRuoliPaSezioneDTO::getCodiceRuolo)
                                        .filter(c -> c != null)
                                        .collect(Collectors.toSet())
                                    : Collections.emptySet();
                                utente.setRuoli(utente.getRuoli().stream()
                                    .filter(ruolo -> ruoliConSezioni.contains(ruolo.getCodiceRuolo()))
                                    .toList());
                            }
                            finalResponse.setData(utente);
                            finalResponse.getStatus().setSuccess(Boolean.TRUE);
                            return finalResponse;
                        }).onErrorResume(err -> {
                            log.warn("Impossibile recuperare sezioni per utente {}: {}", utente.getId(), err.getMessage());
                            finalResponse.setData(utente);
                            finalResponse.getStatus().setSuccess(Boolean.TRUE);
                            return Mono.just(finalResponse);
                        });
                    });
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero profilo da BIP per externalId={}: {}", externalId, e.getMessage(), e);
                GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<List<LabelValueDTO>>> getRuoliByCodePA(String codicePa) {
        log.info("getRuoliByCodePA: codicePa='{}'", codicePa);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<List<LabelValueDTO>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    return Mono.just(errorResponse);
                }

                // Uso la tipologia dell'utente loggato per recuperare i ruoli disponibili
                String tipologiaCorrente = userDTO.getTypeAuthority().toString();
                List<String> tipologia = List.of(tipologiaCorrente);

                HttpHeaders headers = new HttpHeaders();
                headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                headers.setContentType(MediaType.APPLICATION_JSON);

                String uri = UriComponentsBuilder.fromPath("/ruolo/findByTipologia")
                    .queryParamIfPresent("tipologia", Optional.of(tipologia))
                    .build()
                    .toUriString();

                // Prima recupero gli utenti per verificare se esiste già un coordinatore
                return this.findUtentiByPa(codicePa, List.of())
                    .flatMap(utentiResponse -> {
                        List<UtenteRuoloPaDTO> utenti = utentiResponse.getData() != null
                            ? utentiResponse.getData()
                            : Collections.emptyList();

                        boolean hasCoordinatore = utenti.stream()
                            .flatMap(utente -> utente.getRuoli() != null ? utente.getRuoli().stream() : java.util.stream.Stream.empty())
                            .anyMatch(role -> ROLE_COORDINATORE_AMMINISTRATIVO.equals(role.getCodiceRuolo()));

                        return webClientService.get(
                                uri,
                                webServiceType.get(0),
                                headers,
                                new ParameterizedTypeReference<List<RuoloDTO>>() {
                                })
                            .map(res -> {
                                List<LabelValueDTO> ruoli = new ArrayList<>();

                                for (RuoloDTO ruolo : res) {
                                    if (ruolo.getCodRuolo().equals(ROLE_COORDINATORE_AMMINISTRATIVO)) {
                                        if (!hasCoordinatore) {
                                            ruoli.add(new LabelValueDTO(ruolo.getDescrizione(), ruolo.getCodRuolo()));
                                        }
                                    } else if (!ruolo.getCodRuolo().equals(ROLE_REFERENTE)) {
                                        ruoli.add(new LabelValueDTO(ruolo.getDescrizione(), ruolo.getCodRuolo()));
                                    }
                                }

                                GenericResponseDTO<List<LabelValueDTO>> finalResponse = new GenericResponseDTO<>();
                                finalResponse.setStatus(new Status());
                                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                                finalResponse.setData(ruoli);
                                return finalResponse;
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("Errore nel recupero dei ruoli per codicePa={}: {}", codicePa, e.getMessage(), e);
                GenericResponseDTO<List<LabelValueDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    /**
     * Espande ruoli × sezioni: per ogni ruolo dell'utente, crea una riga per ogni sezione.
     * Se le sezioni hanno già codiceRuolo valorizzato, le restituisce così come sono.
     * Altrimenti usa i ruoli dall'oggetto UtenteRuoloPaDTO.
     */
    private List<UtenteRuoliPaSezioneDTO> expandSezioniPerRuolo(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        List<UtenteRuoliPaSezioneDTO> sezioni = utenteRuoloPaDTO.getSezioni();
        List<RuoloUtenteDTO> ruoli = utenteRuoloPaDTO.getRuoli();

        // Se tutte le sezioni hanno già codiceRuolo, non serve espandere
        boolean tutteConRuolo = sezioni.stream()
            .allMatch(s -> s.getCodiceRuolo() != null && !s.getCodiceRuolo().isBlank());
        if (tutteConRuolo) {
            return sezioni;
        }

        // Espansione: per ogni ruolo × ogni sezione = una riga
        List<UtenteRuoliPaSezioneDTO> expanded = new ArrayList<>();
        if (ruoli != null && !ruoli.isEmpty()) {
            for (RuoloUtenteDTO ruolo : ruoli) {
                for (UtenteRuoliPaSezioneDTO sezione : sezioni) {
                    UtenteRuoliPaSezioneDTO row = UtenteRuoliPaSezioneDTO.builder()
                        .strutturaPiao(sezione.getStrutturaPiao())
                        .codiceRuolo(ruolo.getCodiceRuolo())
                        .build();
                    expanded.add(row);
                }
            }
        } else {
            // Nessun ruolo: restituisco le sezioni senza codiceRuolo (il BE darà errore se obbligatorio)
            return sezioni;
        }
        return expanded;
    }

    // =====================================================================
    // Helper: estrae amministrazioneId dal primo elemento di codicePA del DTO
    // =====================================================================

    private String extractAmministrazioneId(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        if (utenteRuoloPaDTO.getCodicePA() != null && !utenteRuoloPaDTO.getCodicePA().isEmpty()) {
            return utenteRuoloPaDTO.getCodicePA().stream()
                .map(UtentePaDTO::getAmministrazioneId)
                .filter(id -> id != null && !id.isBlank())
                .findFirst()
                .orElse("");
        }
        return "";
    }

    // =====================================================================
    // createReferente (senza autenticazione, amministrazioneId in input)
    // =====================================================================

    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> createReferente(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        String amministrazioneId = extractAmministrazioneId(utenteRuoloPaDTO);
        log.info("createReferente: CF={}, amministrazioneId={}", utenteRuoloPaDTO.getCodiceFiscale(), amministrazioneId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Forzo il ruolo Referente nel DTO
        utenteRuoloPaDTO.setRuoli(List.of(RuoloUtenteDTO.builder()
            .codiceRuolo(ROLE_REFERENTE)
            .nomeRuolo("referente")
            .tipologia(TypeAuthority.PA.name())
            .build()));

        // Converto in UserProfileDto per BIP
        UserProfileDto userProfileDto = MapperUtenti.convert(utenteRuoloPaDTO);

        // Setto l'id amministrazione passato in input
        if (userProfileDto.getAmministrazioni() != null) {
            userProfileDto.getAmministrazioni().forEach(amm -> amm.setId(amministrazioneId));
        }

        log.info("createReferente: Chiamata BIP create_sync per CF={}", utenteRuoloPaDTO.getCodiceFiscale());

        return webClientService.post(
                "/utenti/management/user/create_sync",
                webServiceType.get(1), // BIP
                userProfileDto,
                headers,
                new ParameterizedTypeReference<ExternalApiResponse<String>>() {})
            .flatMap(bipResponse -> {
                String externalUserId = bipResponse.getData();
                log.info("createReferente: Utente creato/aggiornato su BIP con id={}", externalUserId);
                utenteRuoloPaDTO.setId(externalUserId);

                // Step 2: Recupero tutte le sezioni effettive dal BE
                return webClientService.get(
                        "/struttura/effective",
                        webServiceType.getFirst(),
                        headers,
                        new ParameterizedTypeReference<List<StrutturaPiaoDTO>>() {})
                    .flatMap(sezioniEffettive -> {
                        log.info("createReferente: Trovate {} sezioni effettive", sezioniEffettive.size());

                        // Step 3: Per ogni sezione effettiva creo una riga con codiceRuolo=ROLE_REFERENTE
                        List<UtenteRuoliPaSezioneDTO> sezioniDaSalvare = sezioniEffettive.stream()
                            .map(struttura -> UtenteRuoliPaSezioneDTO.builder()
                                .strutturaPiao(struttura)
                                .codiceRuolo(ROLE_REFERENTE)
                                .build())
                            .toList();

                        // Step 4: Upsert sezioni sul BE
                        return webClientService.post(
                                "/utente/" + externalUserId + "/sezioni?idAmministrazione=" + amministrazioneId,
                                webServiceType.getFirst(),
                                sezioniDaSalvare,
                                headers,
                                new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {})
                            .map(savedSezioni -> {
                                log.info("createReferente: Salvate {} sezioni per utente id={}", savedSezioni.size(), externalUserId);
                                utenteRuoloPaDTO.setSezioni(savedSezioni);
                                GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                resp.setData(utenteRuoloPaDTO);
                                resp.setStatus(new Status());
                                resp.getStatus().setSuccess(Boolean.TRUE);
                                return resp;
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("createReferente: Errore - {}", e.getMessage(), e);
                GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    // =====================================================================
    // createValidatore (senza autenticazione, amministrazioneId in input)
    // =====================================================================

    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> createValidatore(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        String amministrazioneId = extractAmministrazioneId(utenteRuoloPaDTO);
        log.info("createValidatore: CF={}, amministrazioneId={}", utenteRuoloPaDTO.getCodiceFiscale(), amministrazioneId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Forzo il ruolo Validatore nel DTO
        utenteRuoloPaDTO.setRuoli(List.of(RuoloUtenteDTO.builder()
            .codiceRuolo(ROLE_VALIDATORE)
            .nomeRuolo("validatore")
            .tipologia(TypeAuthority.PA.name())
            .build()));

        // Converto in UserProfileDto per BIP
        UserProfileDto userProfileDto = MapperUtenti.convert(utenteRuoloPaDTO);

        // Setto l'id amministrazione passato in input
        if (userProfileDto.getAmministrazioni() != null) {
            userProfileDto.getAmministrazioni().forEach(amm -> amm.setId(amministrazioneId));
        }

        log.info("createValidatore: Chiamata BIP create_sync per CF={}", utenteRuoloPaDTO.getCodiceFiscale());

        return webClientService.post(
                "/utenti/management/user/create_sync",
                webServiceType.get(1), // BIP
                userProfileDto,
                headers,
                new ParameterizedTypeReference<ExternalApiResponse<String>>() {})
            .flatMap(bipResponse -> {
                String externalUserId = bipResponse.getData();
                log.info("createValidatore: Utente creato/aggiornato su BIP con id={}", externalUserId);
                utenteRuoloPaDTO.setId(externalUserId);

                // Step 2: Recupero tutte le sezioni effettive dal BE
                return webClientService.get(
                        "/struttura/effective",
                        webServiceType.getFirst(),
                        headers,
                        new ParameterizedTypeReference<List<StrutturaPiaoDTO>>() {})
                    .flatMap(sezioniEffettive -> {
                        log.info("createValidatore: Trovate {} sezioni effettive", sezioniEffettive.size());

                        // Step 3: Per ogni sezione effettiva creo una riga con codiceRuolo=ROLE_VALIDATORE
                        List<UtenteRuoliPaSezioneDTO> sezioniDaSalvare = sezioniEffettive.stream()
                            .map(struttura -> UtenteRuoliPaSezioneDTO.builder()
                                .strutturaPiao(struttura)
                                .codiceRuolo(ROLE_VALIDATORE)
                                .build())
                            .toList();

                        // Step 4: Upsert sezioni sul BE
                        return webClientService.post(
                                "/utente/" + externalUserId + "/sezioni?idAmministrazione=" + amministrazioneId,
                                webServiceType.getFirst(),
                                sezioniDaSalvare,
                                headers,
                                new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {})
                            .map(savedSezioni -> {
                                log.info("createValidatore: Salvate {} sezioni per utente id={}", savedSezioni.size(), externalUserId);
                                utenteRuoloPaDTO.setSezioni(savedSezioni);
                                GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                resp.setData(utenteRuoloPaDTO);
                                resp.setStatus(new Status());
                                resp.getStatus().setSuccess(Boolean.TRUE);
                                return resp;
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("createValidatore: Errore - {}", e.getMessage(), e);
                GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    // =====================================================================
    // findUtentiByRuoloAndSezioni
    // =====================================================================

    @Override
    public Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> findUtentiByRuoloAndSezioni(
            String codicePa, List<String> roleNames, List<String> sezioni) {
        log.info("findUtentiByRuoloAndSezioni: codicePa='{}', roleNames={}, sezioni={}", codicePa, roleNames, sezioni);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                Object principal = securityContext.getAuthentication().getPrincipal();
                if (!(principal instanceof UserDTO userDTO)) {
                    log.error("Principal non è un UserDTO: {}", principal.getClass().getName());
                    GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Impossibile recuperare il profilo utente dal contesto di sicurezza");
                    errorResponse.setData(Collections.emptyList());
                    return Mono.just(errorResponse);
                }

                // Recupera amministrazioneId dalla PA corrispondente
                String amministrazioneId = userDTO.getPaRiferimento() != null
                    ? userDTO.getPaRiferimento().stream()
                      .filter(pa -> codicePa != null && codicePa.equals(pa.getCodePA()))
                      .map(PaRiferimentoDTO::getExternalId)
                      .findFirst()
                      .orElse(null)
                    : null;

                if (amministrazioneId == null) {
                    log.warn("Nessun amministrazioneId trovato per codicePa='{}', impossibile chiamare BIP", codicePa);
                    GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                    errorResponse.setStatus(new Status());
                    errorResponse.getStatus().setSuccess(Boolean.FALSE);
                    errorResponse.setError(new Error());
                    errorResponse.getError().setMessageError("Nessuna amministrazione trovata per codicePa=" + codicePa);
                    errorResponse.setData(Collections.emptyList());
                    return Mono.just(errorResponse);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("X-User-Id", userDTO.getFiscalCode());

                // 1) Chiama BIP per recuperare gli utenti con i ruoli specificati
                StringBuilder urlBuilder = new StringBuilder("/utenti/public/amministrazione/list_users?amministrazioneId=")
                    .append(amministrazioneId);
                if (roleNames != null && !roleNames.isEmpty()) {
                    urlBuilder.append("&roleNames=").append(String.join(",", roleNames));
                }
                String url = urlBuilder.toString();

                return webClientService.get(
                        url,
                        webServiceType.get(1), // BIP
                        headers,
                        new ParameterizedTypeReference<ExternalApiResponse<ExternalPageResponse<UserProfileDto>>>() {}
                    )
                    .doOnNext(response -> log.info("findUtentiByRuoloAndSezioni: Risposta BIP ricevuta per codicePa={}", codicePa))
                    .flatMap(bipResponse -> {
                        if (bipResponse == null || bipResponse.getData() == null || bipResponse.getData().getContent() == null) {
                            log.warn("Nessun dato ricevuto da BIP per codicePa='{}'", codicePa);
                            GenericResponseDTO<List<UtenteRuoloPaDTO>> finalResponse = new GenericResponseDTO<>();
                            finalResponse.setStatus(new Status());
                            finalResponse.getStatus().setSuccess(Boolean.TRUE);
                            finalResponse.setData(Collections.emptyList());
                            return Mono.just(finalResponse);
                        }

                        List<UserProfileDto> bipUtenti = bipResponse.getData().getContent();

                        // 2) Per ogni utente BIP, recupera le sezioni assegnate dal BE interno
                        return reactor.core.publisher.Flux.fromIterable(bipUtenti)
                            .flatMap(userProfileDto -> {
                                UtenteRuoloPaDTO utente = MapperUtenti.convert(userProfileDto, codicePa);
                                return webClientService.get(
                                    "/utente/" + utente.getId() + "/sezioni?idAmministrazione=" + amministrazioneId,
                                    webServiceType.getFirst(),
                                    headers,
                                    new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {}
                                ).map(sez -> {
                                    if (sez != null && !sez.isEmpty()) {
                                        utente.setSezioni(sez);
                                    }
                                    return utente;
                                }).onErrorResume(err -> {
                                    log.warn("Impossibile recuperare sezioni per utente {}: {}", utente.getId(), err.getMessage());
                                    return Mono.just(utente);
                                });
                            })
                            .collectList()
                            .map(utenti -> {
                                // 3) Solo per utenti PA: mantieni solo i ruoli BIP che hanno sezioni nel nostro DB
                                boolean isDpf = TypeAuthority.DFP.equals(userDTO.getTypeAuthority());
                                List<UtenteRuoloPaDTO> utentiFiltrati = new ArrayList<>();
                                for (UtenteRuoloPaDTO utente : utenti) {
                                    if (!isDpf && utente.getRuoli() != null) {
                                        Set<String> ruoliConSezioni = utente.getSezioni() != null
                                            ? utente.getSezioni().stream()
                                                .map(UtenteRuoliPaSezioneDTO::getCodiceRuolo)
                                                .filter(c -> c != null)
                                                .collect(Collectors.toSet())
                                            : Collections.emptySet();
                                        utente.setRuoli(utente.getRuoli().stream()
                                            .filter(ruolo -> ruoliConSezioni.contains(ruolo.getCodiceRuolo()))
                                            .toList());
                                    }
                                    if (utente.getRuoli() != null && !utente.getRuoli().isEmpty()) {
                                        utentiFiltrati.add(utente);
                                    }
                                }

                                // 4) Se sono state specificate sezioni, filtra solo gli utenti che hanno
                                //    almeno un ruolo richiesto in almeno una delle sezioni indicate
                                List<UtenteRuoloPaDTO> risultato;
                                if (sezioni != null && !sezioni.isEmpty()) {
                                    risultato = utentiFiltrati.stream()
                                        .filter(utente -> utente.getSezioni() != null && utente.getSezioni().stream()
                                            .anyMatch(sez -> {
                                                // Verifica che il codiceRuolo della sezione sia tra i roleNames richiesti
                                                boolean ruoloMatch = roleNames == null || roleNames.isEmpty()
                                                    || (sez.getCodiceRuolo() != null && roleNames.contains(sez.getCodiceRuolo()));
                                                // Verifica che la sezione (numeroSezione struttura) sia tra quelle richieste
                                                boolean sezioneMatch = sez.getStrutturaPiao() != null
                                                    && sez.getStrutturaPiao().getNumeroSezione() != null
                                                    && sezioni.contains(sez.getStrutturaPiao().getNumeroSezione());
                                                return ruoloMatch && sezioneMatch;
                                            }))
                                        .toList();
                                    log.info("findUtentiByRuoloAndSezioni: {} utenti dopo filtro sezioni", risultato.size());
                                } else {
                                    // Nessun filtro sezioni, restituisci tutti quelli con i ruoli richiesti
                                    risultato = utentiFiltrati;
                                }

                                GenericResponseDTO<List<UtenteRuoloPaDTO>> finalResponse = new GenericResponseDTO<>();
                                finalResponse.setData(risultato);
                                finalResponse.setStatus(new Status());
                                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                                return finalResponse;
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("Errore in findUtentiByRuoloAndSezioni per codicePa={}: {}", codicePa, e.getMessage(), e);
                GenericResponseDTO<List<UtenteRuoloPaDTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                errorResponse.setData(Collections.emptyList());
                return Mono.just(errorResponse);
            });
    }

    // =====================================================================
    // createRedattore (senza autenticazione, amministrazioneId in input)
    // =====================================================================

    @Override
    public Mono<GenericResponseDTO<UtenteRuoloPaDTO>> createRedattore(UtenteRuoloPaDTO utenteRuoloPaDTO) {
        String amministrazioneId = extractAmministrazioneId(utenteRuoloPaDTO);
        log.info("createRedattore: CF={}, amministrazioneId={}", utenteRuoloPaDTO.getCodiceFiscale(), amministrazioneId);

        final String ROLE_REDATTORE = "ROLE_REDATTORE";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Forzo il ruolo Redattore nel DTO
        utenteRuoloPaDTO.setRuoli(List.of(RuoloUtenteDTO.builder()
            .codiceRuolo(ROLE_REDATTORE)
            .nomeRuolo("redattore")
            .tipologia(TypeAuthority.PA.name())
            .build()));

        // Converto in UserProfileDto per BIP
        UserProfileDto userProfileDto = MapperUtenti.convert(utenteRuoloPaDTO);

        // Setto l'id amministrazione passato in input
        if (userProfileDto.getAmministrazioni() != null) {
            userProfileDto.getAmministrazioni().forEach(amm -> amm.setId(amministrazioneId));
        }

        log.info("createRedattore: Chiamata BIP create_sync per CF={}", utenteRuoloPaDTO.getCodiceFiscale());

        return webClientService.post(
                "/utenti/management/user/create_sync",
                webServiceType.get(1), // BIP
                userProfileDto,
                headers,
                new ParameterizedTypeReference<ExternalApiResponse<String>>() {})
            .flatMap(bipResponse -> {
                String externalUserId = bipResponse.getData();
                log.info("createRedattore: Utente creato/aggiornato su BIP con id={}", externalUserId);
                utenteRuoloPaDTO.setId(externalUserId);

                // Step 2: Recupero tutte le sezioni effettive dal BE
                return webClientService.get(
                        "/struttura/effective",
                        webServiceType.getFirst(),
                        headers,
                        new ParameterizedTypeReference<List<StrutturaPiaoDTO>>() {})
                    .flatMap(sezioniEffettive -> {
                        log.info("createRedattore: Trovate {} sezioni effettive", sezioniEffettive.size());

                        // Step 3: Per ogni sezione effettiva creo una riga con codiceRuolo=ROLE_REDATTORE
                        List<UtenteRuoliPaSezioneDTO> sezioniDaSalvare = sezioniEffettive.stream()
                            .map(struttura -> UtenteRuoliPaSezioneDTO.builder()
                                .strutturaPiao(struttura)
                                .codiceRuolo(ROLE_REDATTORE)
                                .build())
                            .toList();

                        // Step 4: Upsert sezioni sul BE
                        return webClientService.post(
                                "/utente/" + externalUserId + "/sezioni?idAmministrazione=" + amministrazioneId,
                                webServiceType.getFirst(),
                                sezioniDaSalvare,
                                headers,
                                new ParameterizedTypeReference<List<UtenteRuoliPaSezioneDTO>>() {})
                            .map(savedSezioni -> {
                                log.info("createRedattore: Salvate {} sezioni per utente id={}", savedSezioni.size(), externalUserId);
                                utenteRuoloPaDTO.setSezioni(savedSezioni);
                                GenericResponseDTO<UtenteRuoloPaDTO> resp = new GenericResponseDTO<>();
                                resp.setData(utenteRuoloPaDTO);
                                resp.setStatus(new Status());
                                resp.getStatus().setSuccess(Boolean.TRUE);
                                return resp;
                            });
                    });
            })
            .onErrorResume(e -> {
                log.error("createRedattore: Errore - {}", e.getMessage(), e);
                GenericResponseDTO<UtenteRuoloPaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

}
