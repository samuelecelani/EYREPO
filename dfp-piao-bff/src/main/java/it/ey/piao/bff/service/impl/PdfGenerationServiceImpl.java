package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.KeyNotification;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.StatusAllegato;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfGenerationServiceImpl implements IPdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType notificaBeType;
    private final ISezione1Service sezione1Service;
    private final ISezione21Service sezione21Service;
    private final ISezione22Service sezione22Service;
    private final ISezione23Service sezione23Service;
    private final ISezione31Service sezione31Service;
    private final ISezione32Service sezione32Service;
    private final ISezione331Service sezione331Service;
    private final ISezione332Service sezione332Service;
    private final ISezione4Service sezione4Service;
    private final IPiaoService piaoService;
    private final IOVPService ovpService;
    private final IAllegatoService allegatoService;
    private final IConfigurazioniService configurazioniService;

    public PdfGenerationServiceImpl(WebClientService webClientService,
                                    ISezione1Service sezione1Service,
                                    ISezione21Service sezione21Service,
                                    ISezione22Service sezione22Service,
                                    ISezione23Service sezione23Service,
                                    ISezione31Service sezione31Service,
                                    ISezione32Service sezione32Service,
                                    ISezione331Service sezione331Service,
                                    ISezione332Service sezione332Service,
                                    ISezione4Service sezione4Service,
                                    IPiaoService piaoService,
                                    IOVPService ovpService,
                                    IAllegatoService allegatoService,
                                    IConfigurazioniService configurazioniService) {
        this.webClientService = webClientService;
        this.notificaBeType = WebServiceType.NOTIFICATION_BE;
        this.sezione1Service = sezione1Service;
        this.sezione21Service = sezione21Service;
        this.sezione22Service = sezione22Service;
        this.sezione23Service = sezione23Service;
        this.sezione31Service = sezione31Service;
        this.sezione32Service = sezione32Service;
        this.sezione331Service = sezione331Service;
        this.sezione332Service = sezione332Service;
        this.sezione4Service = sezione4Service;
        this.piaoService = piaoService;
        this.ovpService = ovpService;
        this.allegatoService = allegatoService;
        this.configurazioniService = configurazioniService;
    }

    @Override
    public Mono<GenericResponseDTO<PdfNotificationDTO>> generatePdf(Long idPiao, Sezione sezione, String codicePa) {
        log.info("Richiesta generazione PDF per idPiao={}, sezione={}, codicePa={}", idPiao, sezione, codicePa);

        // Cattura il SecurityContext ORA, prima che la catena asincrona lo perda
        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                // 1) Recupera il PIAO
                return loadPiao(idPiao, codicePa)
                    .flatMap(dataMap -> {
                        // 2) Carica le sezioni
                        Mono<Map<String, Object>> dataMono = sezione.equals(Sezione.PIAO)
                            ? loadAllSezioni(idPiao, dataMap)
                            : loadSingleSezione(idPiao, sezione, dataMap);

                        return dataMono.flatMap(d -> sendPdfGeneration(idPiao, sezione, codicePa, d, securityContext));
                    });
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.error("SecurityContext assente all'ingresso di generatePdf per idPiao={}, sezione={}", idPiao, sezione);
                GenericResponseDTO<PdfNotificationDTO> errResp = new GenericResponseDTO<>();
                errResp.setStatus(new Status());
                errResp.getStatus().setSuccess(Boolean.FALSE);
                errResp.setError(new Error());
                errResp.getError().setMessageError("SecurityContext non disponibile: utente non autenticato");
                return Mono.just(errResp);
            }));
    }

    /**
     * Recupera il PIAO per codice PA e filtra per idPiao.
     * Restituisce una mappa iniziale con idPiao e i dati del PIAO.
     */
    private Mono<Map<String, Object>> loadPiao(Long idPiao, String codicePa) {
        return piaoService.findById(idPiao)
            .flatMap(response -> {
                Map<String, Object> dataMap = new LinkedHashMap<>();
                dataMap.put("idPiao", idPiao);
                if (response.getData() != null) {
                    dataMap.put("piao", response.getData());
                } else {
                    log.warn("PIAO con id={} non trovato", idPiao);
                }
                return Mono.just(dataMap);
            })
            .onErrorResume(e -> {
                log.warn("Errore recupero PIAO per id={}: {}", idPiao, e.getMessage());
                Map<String, Object> dataMap = new LinkedHashMap<>();
                dataMap.put("idPiao", idPiao);
                return Mono.just(dataMap);
            });
    }

    private static final Object EMPTY_PLACEHOLDER = new Object();

    private Mono<Map<String, Object>> loadAllSezioni(Long idPiao, Map<String, Object> dataMap) {
        return Mono.zip(
            safeGetData(sezione1Service.findByPiao(idPiao,null)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione21Service.findByPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione22Service.findByPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione23Service.findByIdPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione31Service.findByPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione32Service.findByPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione331Service.findByPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER),
            safeGetData(sezione332Service.findByPiao(idPiao)).cast(Object.class).defaultIfEmpty(EMPTY_PLACEHOLDER)
        ).flatMap(tuple -> {
            if (tuple.getT1() != EMPTY_PLACEHOLDER) dataMap.put("sezione1", tuple.getT1());
            if (tuple.getT2() != EMPTY_PLACEHOLDER) dataMap.put("sezione21", tuple.getT2());
            if (tuple.getT3() != EMPTY_PLACEHOLDER) dataMap.put("sezione22", tuple.getT3());
            if (tuple.getT4() != EMPTY_PLACEHOLDER) dataMap.put("sezione23", tuple.getT4());
            if (tuple.getT5() != EMPTY_PLACEHOLDER) dataMap.put("sezione31", tuple.getT5());
            if (tuple.getT6() != EMPTY_PLACEHOLDER) dataMap.put("sezione32", tuple.getT6());
            if (tuple.getT7() != EMPTY_PLACEHOLDER) dataMap.put("sezione331", tuple.getT7());
            if (tuple.getT8() != EMPTY_PLACEHOLDER) dataMap.put("sezione332", tuple.getT8());

            // Flag booleani per il template Jasper: tutte le sezioni visibili
            dataMap.put("renderSezione1", Boolean.TRUE);
            dataMap.put("renderSezione21", Boolean.TRUE);
            dataMap.put("renderSezione22", Boolean.TRUE);
            dataMap.put("renderSezione23", Boolean.TRUE);
            dataMap.put("renderSezione31", Boolean.TRUE);
            dataMap.put("renderSezione32", Boolean.TRUE);
            dataMap.put("renderSezione331", Boolean.TRUE);
            dataMap.put("renderSezione332", Boolean.TRUE);
            dataMap.put("renderSezione4", Boolean.TRUE);
            dataMap.put("renderSommario", Boolean.TRUE);
            dataMap.put("renderCover", Boolean.TRUE);

            return safeGetData(sezione4Service.findByIdPiao(idPiao))
                .map(sez4 -> {
                    dataMap.put("sezione4", sez4);
                    return dataMap;
                })
                .defaultIfEmpty(dataMap)
                .flatMap(dm -> loadMatriceOVP(dm, idPiao));
        });
    }

    /**
     * Recupera la matrice OVP agganciata a sezione1 e sezione21 e la inserisce nella mappa.
     * Richiede che sezione1 e sezione21 siano già presenti in {@code dataMap}.
     */
    private Mono<Map<String, Object>> loadMatriceOVP(Map<String, Object> dataMap, Long idPiao) {
        Object sez1 = dataMap.get("sezione1");
        Object sez21 = dataMap.get("sezione21");
        Long idSezione1 = (sez1 instanceof Sezione1DTO s1) ? s1.getId() : null;
        Long idSezione21 = (sez21 instanceof Sezione21DTO s21) ? s21.getId() : null;

        if (idSezione1 == null) {
            log.warn("Sezione1 non disponibile, skip matrice OVP per idPiao={}", idPiao);
            return Mono.just(dataMap);
        }

        return safeGetData(ovpService.getOvpMatriceByIdSezione21(idSezione21, idSezione1, idPiao))
            .map(matrice -> {
                dataMap.put("matriceOVP", matrice);
                return dataMap;
            })
            .defaultIfEmpty(dataMap);
    }

    /**
     * Carica la sezione richiesta insieme alle sue dipendenze cross-sezione.
     * Es. la sezione1 e la sezione21 sono legate dalla matrice OVP: se ne richiedo una,
     * carico comunque anche l'altra (e la matrice OVP).
     */
    private Mono<Map<String, Object>> loadSingleSezione(Long idPiao, Sezione sezione, Map<String, Object> dataMap) {

        // Flag booleani: solo la sezione richiesta è visibile
        dataMap.put("renderSezione1", sezione.equals(Sezione.SEZIONE_1));
        dataMap.put("renderSezione21", sezione.equals(Sezione.SEZIONE_21));
        dataMap.put("renderSezione22", sezione.equals(Sezione.SEZIONE_22));
        dataMap.put("renderSezione23", sezione.equals(Sezione.SEZIONE_23));
        dataMap.put("renderSezione3", sezione.equals(Sezione.SEZIONE_31) ||
            sezione.equals(Sezione.SEZIONE_32) || sezione.equals(Sezione.SEZIONE_331) || sezione.equals(Sezione.SEZIONE_332));
        dataMap.put("renderSezione4", sezione.equals(Sezione.SEZIONE_4));
        dataMap.put("renderSommario", Boolean.FALSE);
        dataMap.put("renderCover", Boolean.FALSE);

        // Carica in parallelo la sezione richiesta + eventuali sezioni dipendenti
        return loadSezioneConDipendenze(idPiao, sezione, dataMap);
    }

    /**
     * Carica la sezione principale e tutte le sezioni dipendenti necessarie alla resa del PDF,
     * inclusi i dati derivati (es. matrice OVP).
     */
    private Mono<Map<String, Object>> loadSezioneConDipendenze(Long idPiao, Sezione sezione, Map<String, Object> dataMap) {
        // Determina l'insieme di sezioni da caricare (principale + dipendenze)
        java.util.Set<Sezione> sezioniDaCaricare = new java.util.LinkedHashSet<>();
        sezioniDaCaricare.add(sezione);
        sezioniDaCaricare.addAll(dipendenzeDi(sezione));

        // Lancia i caricamenti in parallelo
        List<Mono<Map.Entry<String, Object>>> caricamenti = sezioniDaCaricare.stream()
            .map(s -> loadSezioneById(idPiao, s)
                .map(dto -> (Map.Entry<String, Object>) new java.util.AbstractMap.SimpleEntry<>(resolveKey(s), (Object) dto)))
            .toList();

        Mono<Map<String, Object>> base = caricamenti.isEmpty()
            ? Mono.just(dataMap)
            : Mono.zip(caricamenti, results -> {
                for (Object r : results) {
                    @SuppressWarnings("unchecked")
                    Map.Entry<String, Object> entry = (Map.Entry<String, Object>) r;
                    dataMap.put(entry.getKey(), entry.getValue());
                }
                return dataMap;
            });

        // Se la sezione richiesta (o una sua dipendenza) coinvolge la matrice OVP, caricala
        boolean richiedeMatriceOVP = sezioniDaCaricare.contains(Sezione.SEZIONE_1)
            || sezioniDaCaricare.contains(Sezione.SEZIONE_21);

        return base.flatMap(dm -> richiedeMatriceOVP ? loadMatriceOVP(dm, idPiao) : Mono.just(dm));
    }

    /**
     * Mappa delle dipendenze cross-sezione per la generazione PDF.
     */
    private java.util.Set<Sezione> dipendenzeDi(Sezione sezione) {
        return switch (sezione) {
            // La matrice OVP collega sezione1 e sezione21 -> ciascuna richiede l'altra
            case SEZIONE_1  -> java.util.Set.of(Sezione.SEZIONE_21);
            case SEZIONE_21 -> java.util.Set.of(Sezione.SEZIONE_1);
            case SEZIONE_22 -> java.util.Set.of(Sezione.SEZIONE_21);
            case SEZIONE_23 -> java.util.Set.of(Sezione.SEZIONE_21,Sezione.SEZIONE_22);

            default -> java.util.Set.of();
        };
    }

    /**
     * Carica i dati di una singola sezione tramite il rispettivo service.
     */
    private Mono<?> loadSezioneById(Long idPiao, Sezione sezione) {
        return switch (sezione) {
            case SEZIONE_1   -> safeGetData(sezione1Service.findByPiao(idPiao, null));
            case SEZIONE_21  -> safeGetData(sezione21Service.findByPiao(idPiao));
            case SEZIONE_22  -> safeGetData(sezione22Service.findByPiao(idPiao));
            case SEZIONE_23  -> safeGetData(sezione23Service.findByIdPiao(idPiao));
            case SEZIONE_31  -> safeGetData(sezione31Service.findByPiao(idPiao));
            case SEZIONE_32  -> safeGetData(sezione32Service.findByPiao(idPiao));
            case SEZIONE_331 -> safeGetData(sezione331Service.findByPiao(idPiao));
            case SEZIONE_332 -> safeGetData(sezione332Service.findByPiao(idPiao));
            case SEZIONE_4   -> safeGetData(sezione4Service.findByIdPiao(idPiao));
            default -> Mono.empty();
        };
    }

    private String resolveKey(Sezione sezione) {
        return switch (sezione) {
            case SEZIONE_1   -> "sezione1";
            case SEZIONE_21  -> "sezione21";
            case SEZIONE_22  -> "sezione22";
            case SEZIONE_23  -> "sezione23";
            case SEZIONE_31  -> "sezione31";
            case SEZIONE_32  -> "sezione32";
            case SEZIONE_331 -> "sezione331";
            case SEZIONE_332 -> "sezione332";
            case SEZIONE_4   -> "sezione4";
            default -> sezione.name().toLowerCase();
        };
    }

    private Mono<GenericResponseDTO<PdfNotificationDTO>> sendPdfGeneration(Long idPiao, Sezione sezione, String codicePa,
                                                                              Map<String, Object> dataMap,
                                                                              org.springframework.security.core.context.SecurityContext securityContext) {
        // Recupera l'amministrazioneId (externalId) dal UserDTO nel SecurityContext
        String amministrazioneId = null;
        String codiceFiscale = null;
        String nomeCompleto = null;
        String ruoloUtente = null;
        Object principal = securityContext.getAuthentication().getPrincipal();
        if (principal instanceof UserDTO userDTO && userDTO.getPaRiferimento() != null) {
            PaRiferimentoDTO paFiltrata = userDTO.getPaRiferimento().stream()
                .filter(pa -> codicePa != null && codicePa.equals(pa.getCodePA()))
                .findFirst()
                .orElse(null);

            if (paFiltrata != null) {
                amministrazioneId = paFiltrata.getExternalId();
                // Recupera il ruolo attivo dalla PA filtrata
                if (paFiltrata.getRuoli() != null) {
                    ruoloUtente = paFiltrata.getRuoli().stream()
                        .map(RuoloUserDTO::getCodice)
                        .findFirst()
                        .orElse(null);
                }
            }

            codiceFiscale = userDTO.getFiscalCode();
            nomeCompleto = ((userDTO.getNome() != null ? userDTO.getNome() : "") + " " +
                            (userDTO.getCognome() != null ? userDTO.getCognome() : "")).trim();
        }
        if (amministrazioneId == null) {
            log.warn("Nessun amministrazioneId trovato per codicePa='{}' dal SecurityContext", codicePa);
        }

        // Ruoli destinatari della notifica PDF

        String defaultMessage = "Generazione PDF per " + sezione.name() + " - PIAO id=" + idPiao;

        PdfNotificationDTO pdfNotification = new PdfNotificationDTO();
        pdfNotification.setTemplateName(sezione.name().toLowerCase());
        pdfNotification.setData(dataMap);
        if(Sezione.PIAO.equals(sezione)) {
            pdfNotification.setOutputFileName(sezione.name().toLowerCase() + "_" + idPiao + ".pdf");
        } else {
            pdfNotification.setOutputFileName("BOZZA_"+sezione.name().toLowerCase() + "_" + idPiao + ".pdf");
        }
        pdfNotification.setIdModulo("PIAO");
        pdfNotification.setMessage(defaultMessage);
        pdfNotification.setRuoli(List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()));
        pdfNotification.setCodicePa(codicePa);
        pdfNotification.setAmministrazioneId(amministrazioneId);
        pdfNotification.setCodiceFiscaleChiamante(codiceFiscale);
        pdfNotification.setNomeModulo("PIAO");

        // 1) Costruisci l'allegato temporaneo con i campi obbligatori
        AllegatoDTO tempAllegato = AllegatoDTO.builder()
            .idEntitaFK(idPiao)
            .isDoc(true)
            .codDocumento(pdfNotification.getOutputFileName())
            .codTipologiaFK(sezione.name())
            .codTipologiaAllegato(CodTipologiaAllegato.PIAO_PDF_GENERATO.name())
            .descrizione("PDF " + pdfNotification.getTemplateName() + " - in generazione")
            .type(sezione.name())
            .statusAllegato(StatusAllegato.IN_GENERAZIONE.name())
            .active(true)
            .build();

        // Campi tecnici dall'utente in sessione
        tempAllegato.setCreatedBy(codiceFiscale);
        tempAllegato.setCreatedByNameSurname(nomeCompleto);
        tempAllegato.setCreatedByRole(ruoloUtente);
        tempAllegato.setValidity(true);

        log.info("Salvataggio allegato temporaneo per idPiao={}, sezione={}", idPiao, sezione);

        // Recupera il messaggio dalla configurazione DB, poi salva allegato e invia notifica
        return configurazioniService.getConfigurazioneByCodice(KeyNotification.NOTIFICA_GENERA_PDF.name())
            .onErrorResume(e -> {
                log.warn("Errore recupero configurazione {}: {}", KeyNotification.NOTIFICA_GENERA_PDF.name(), e.getMessage());
                return Mono.just(new GenericResponseDTO<>());
            })
            .defaultIfEmpty(new GenericResponseDTO<>())
            .flatMap(configResponse -> {
                if (configResponse.getData() != null && configResponse.getData().getValore() != null) {
                    pdfNotification.setMessage(configResponse.getData().getValore());
                    log.info("Messaggio notifica PDF caricato da configurazione: {}", configResponse.getData().getValore());
                } else {
                    log.info("Configurazione {} non trovata, uso messaggio di default", KeyNotification.NOTIFICA_GENERA_PDF.name());
                }

                // 2) Salva l'allegato temporaneo, ottieni l'ID, settalo sul DTO e poi invia la notifica
                return allegatoService.saveAllegatoSenzaUpload(tempAllegato)
                    .flatMap(allegatoResponse -> {
                        if (allegatoResponse.getData() != null && allegatoResponse.getData().getId() != null) {
                            AllegatoDTO savedAllegato = allegatoResponse.getData();
                            log.info("Allegato temporaneo salvato con id={} per idPiao={}", savedAllegato.getId(), idPiao);
                            pdfNotification.setAllegato(savedAllegato);
                        } else {
                            log.warn("Allegato temporaneo salvato ma senza ID per idPiao={}", idPiao);
                            pdfNotification.setAllegato(tempAllegato);
                        }

                        return sendNotification(pdfNotification, idPiao, sezione);
                    })
                    .onErrorResume(e -> {
                        log.error("Errore salvataggio allegato temporaneo per idPiao={}: {}", idPiao, e.getMessage(), e);
                        // Procedi comunque con la notifica senza allegato pre-salvato
                        pdfNotification.setAllegato(tempAllegato);
                        return sendNotification(pdfNotification, idPiao, sezione);
                    });
            });
    }

    /**
     * Invia la richiesta di generazione PDF al Notifica_BE.
     */
    private Mono<GenericResponseDTO<PdfNotificationDTO>> sendNotification(PdfNotificationDTO pdfNotification, Long idPiao, Sezione sezione) {
        log.info("Invio richiesta generazione PDF al Notifica_BE: templateName={}, idPiao={}, allegatoId={}, dataKeys={}",
            pdfNotification.getTemplateName(), idPiao,
            pdfNotification.getAllegato() != null ? pdfNotification.getAllegato().getId() : null,
            pdfNotification.getData() != null ? pdfNotification.getData().keySet() : "null");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post(
            "/api/v1/pdf/generation",
            notificaBeType,
            pdfNotification,
            headers,
            new ParameterizedTypeReference<GenericResponseDTO<PdfNotificationDTO>>() {}
        ).doOnNext(r -> log.info("Richiesta generazione PDF inviata con successo per idPiao={}, sezione={}", idPiao, sezione))
         .onErrorResume(e -> {
             log.error("Errore invio richiesta generazione PDF: {}", e.getMessage(), e);
             GenericResponseDTO<PdfNotificationDTO> errResp = new GenericResponseDTO<>();
             errResp.setStatus(new Status());
             errResp.getStatus().setSuccess(Boolean.FALSE);
             errResp.setError(new Error());
             errResp.getError().setMessageError(e.getMessage());
             return Mono.just(errResp);
         });
    }

    private <T> Mono<T> safeGetData(Mono<GenericResponseDTO<T>> mono) {
        return mono
            .filter(r -> r.getData() != null)
            .map(GenericResponseDTO::getData)
            .onErrorResume(e -> {
                log.warn("Errore recupero sezione, verrà ignorata: {}", e.getMessage());
                return Mono.empty();
            });
    }
}
