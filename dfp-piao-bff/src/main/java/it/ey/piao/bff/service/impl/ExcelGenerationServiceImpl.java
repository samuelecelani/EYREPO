package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.dto.external.PiaoExcelDTO;
import it.ey.dto.external.PiaoExternalDTO;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.KeyNotification;
import it.ey.enums.Ruolo;
import it.ey.enums.StatusAllegato;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IAllegatoService;
import it.ey.piao.bff.service.IConfigurazioniService;
import it.ey.piao.bff.service.IExcelGenerationService;
import it.ey.piao.bff.service.IPiaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelGenerationServiceImpl implements IExcelGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ExcelGenerationServiceImpl.class);

    private final WebClientService webClientService;
    private final WebServiceType notificaBeType;
    private final IPiaoService piaoService;
    private final IAllegatoService allegatoService;
    private final IConfigurazioniService configurazioniService;

    public ExcelGenerationServiceImpl(WebClientService webClientService,
                                      IPiaoService piaoService,
                                      IAllegatoService allegatoService,
                                      IConfigurazioniService configurazioniService) {
        this.webClientService = webClientService;
        this.notificaBeType = WebServiceType.NOTIFICATION_BE;
        this.piaoService = piaoService;
        this.allegatoService = allegatoService;
        this.configurazioniService = configurazioniService;
    }

    @Override
    public Mono<GenericResponseDTO<List<ExcelNotificationDTO>>> generateExcelBatch(List<Long> idPiaoList, String codicePa) {
        log.info("Richiesta generazione Excel batch per idPiaoList={}, codicePa={}", idPiaoList, codicePa);

        return ReactiveSecurityContextHolder.getContext()
            .flatMap(securityContext -> {
                // Estrai informazioni dell'utente dal SecurityContext
                String amministrazioneId = null;
                String codiceFiscale = null;
                String nomeCompleto = null;
                String ruoloUtente = null;

                Object principal = securityContext.getAuthentication().getPrincipal();
                if (principal instanceof UserDTO userDTO && userDTO.getPaRiferimento() != null) {
                    PaRiferimentoDTO paFiltrata = null;

                    if (codicePa != null && !codicePa.isBlank()) {
                        // Filtra per codicePa specifico
                        paFiltrata = userDTO.getPaRiferimento().stream()
                            .filter(pa -> codicePa.equals(pa.getCodePA()))
                            .findFirst()
                            .orElse(null);
                    } else {
                        // Se codicePa non fornito, prendi la prima PA disponibile
                        paFiltrata = userDTO.getPaRiferimento().stream()
                            .findFirst()
                            .orElse(null);
                    }

                    if (paFiltrata != null) {
                        amministrazioneId = paFiltrata.getExternalId();
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

                final String finalAmministrazioneId = amministrazioneId;
                final String finalCodiceFiscale = codiceFiscale;
                final String finalNomeCompleto = nomeCompleto;
                final String finalRuoloUtente = ruoloUtente;

                // Recupera i dati PiaoExternal e PiaoDTO per tutti gli idPiao dal BE
                Mono<GenericResponseDTO<List<PiaoExternalDTO>>> externalMono = piaoService.findPiaoExternalByIds(idPiaoList);
                Mono<GenericResponseDTO<List<PiaoDTO>>> piaoMono = (codicePa != null && !codicePa.isBlank())
                    // TODO sostituire con il nuovo metodo del getPIAO dalla tipologia passata
                    // reworkare successivamente
                    ? piaoService.findByCodPaFkAndIsCurrent(codicePa, false)
                    : Mono.just(new GenericResponseDTO<>());

                return Mono.zip(externalMono, piaoMono)
                    .flatMap(tuple -> {
                        GenericResponseDTO<List<PiaoExternalDTO>> externalResponse = tuple.getT1();
                        List<PiaoDTO> piaoList = tuple.getT2().getData() != null ? tuple.getT2().getData() : List.of();

                        // Filtra solo i PiaoDTO con ID nella idPiaoList
                        Map<Long, PiaoDTO> piaoMap = piaoList.stream()
                            .filter(p -> p.getId() != null && idPiaoList.contains(p.getId()))
                            .collect(Collectors.toMap(PiaoDTO::getId, p -> p, (a, b) -> a));

                        return processExternalResponse(
                            externalResponse, idPiaoList, codicePa,
                            finalAmministrazioneId, finalCodiceFiscale,
                            finalNomeCompleto, finalRuoloUtente, piaoMap
                        );
                    });
            })
            .switchIfEmpty(Mono.defer(() -> {
                log.error("SecurityContext assente per la generazione Excel batch");
                GenericResponseDTO<List<ExcelNotificationDTO>> errResp = new GenericResponseDTO<>();
                errResp.setStatus(new Status());
                errResp.getStatus().setSuccess(Boolean.FALSE);
                errResp.setError(new Error());
                errResp.getError().setMessageError("SecurityContext non disponibile: utente non autenticato");
                return Mono.just(errResp);
            }));
    }

    /**
     * Processa la risposta dal BE contenente la lista di PiaoExternalDTO.
     * Costruisce UN UNICO ExcelNotificationDTO con dataList (un elemento per PIAO = un sheet),
     * arricchisce i dati con codPAFK, denominazione e tipologia dal PiaoDTO,
     * crea l'allegato temporaneo e invia al Notifica_BE.
     */
    private Mono<GenericResponseDTO<List<ExcelNotificationDTO>>> processExternalResponse(
            GenericResponseDTO<List<PiaoExternalDTO>> response,
            List<Long> idPiaoList, String codicePa,
            String amministrazioneId, String codiceFiscale,
            String nomeCompleto, String ruoloUtente,
            Map<Long, PiaoDTO> piaoMap) {

        if (response.getData() == null || response.getData().isEmpty()) {
            log.warn("Nessun PIAO External trovato per idPiaoList={}", idPiaoList);
            GenericResponseDTO<List<ExcelNotificationDTO>> errResp = new GenericResponseDTO<>();
            errResp.setStatus(new Status());
            errResp.getStatus().setSuccess(Boolean.FALSE);
            errResp.setError(new Error());
            errResp.getError().setMessageError("Nessun PIAO trovato per gli ID forniti");
            return Mono.just(errResp);
        }

        List<PiaoExternalDTO> piaoExternals = response.getData();

        // Costruisci la dataList: un elemento per ogni PIAO (= un sheet nell'Excel)
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < piaoExternals.size(); i++) {
            PiaoExternalDTO piaoExternal = piaoExternals.get(i);
            Long idPiao = idPiaoList.size() > i ? idPiaoList.get(i) : null;

            // Costruisci PiaoExcelDTO arricchito con i dati del PiaoDTO
            PiaoExcelDTO piaoExcel = new PiaoExcelDTO();
            piaoExcel.setAnagrafica(piaoExternal.getAnagrafica());
            piaoExcel.setOvp(piaoExternal.getOvp());
            piaoExcel.setPopolazioneSuddivisaEta(piaoExternal.getPopolazioneSuddivisaEta());

            if (idPiao != null && piaoMap.containsKey(idPiao)) {
                PiaoDTO piao = piaoMap.get(idPiao);
                piaoExcel.setCodPAFK(piao.getCodPAFK());
                piaoExcel.setDenominazione(piao.getDenominazione());
                piaoExcel.setTipologia(piao.getTipologia());
            }

            Map<String, Object> sheetData = new LinkedHashMap<>();
            sheetData.put("idPiao", idPiao);
            sheetData.put("piaoExcel", piaoExcel);

            if (piaoExcel.getAnagrafica() != null) {
                sheetData.put("anagrafica", piaoExcel.getAnagrafica());
            }
            if (piaoExcel.getOvp() != null) {
                sheetData.put("ovp", piaoExcel.getOvp());
            }
            if (piaoExcel.getPopolazioneSuddivisaEta() != null) {
                sheetData.put("popolazioneSuddivisaEta", piaoExcel.getPopolazioneSuddivisaEta());
            }
            if (piaoExcel.getCodPAFK() != null) {
                sheetData.put("codPAFK", piaoExcel.getCodPAFK());
            }
            if (piaoExcel.getDenominazione() != null) {
                sheetData.put("denominazione", piaoExcel.getDenominazione());
            }
            if (piaoExcel.getTipologia() != null) {
                sheetData.put("tipologia", piaoExcel.getTipologia());
            }

            dataList.add(sheetData);
        }

        // Costruisci UN UNICO ExcelNotificationDTO con tutti i PIAO nella dataList
        ExcelNotificationDTO excelNotification = new ExcelNotificationDTO();
        excelNotification.setTemplateName("piao_excel");
        excelNotification.setNomeModulo("PIAO");
        excelNotification.setIdModulo("PIAO");
        excelNotification.setMessage("Generazione Excel batch per " + idPiaoList.size() + " PIAO - ids=" + idPiaoList);
        excelNotification.setOutputFileName("piao_batch_" + String.join("_", idPiaoList.stream().map(String::valueOf).toList()) + ".xlsx");
        excelNotification.setRuoli(List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()));
        excelNotification.setCodicePa(codicePa);
        excelNotification.setAmministrazioneId(amministrazioneId);
        excelNotification.setCodiceFiscaleChiamante(codiceFiscale);
        excelNotification.setUploadFile(true);
        excelNotification.setData(dataList);


        // Costruisci l'allegato temporaneo
        AllegatoDTO tempAllegato = AllegatoDTO.builder()
            .idEntitaFK(idPiaoList.get(0))
            .isDoc(true)
            .codDocumento(excelNotification.getOutputFileName())
            .codTipologiaFK("PIAO")
            .codTipologiaAllegato(CodTipologiaAllegato.PIAO_EXCEL_GENERATO.name())
            .descrizione("Excel " + excelNotification.getTemplateName() + " - " + idPiaoList.size() + " PIAO - in generazione")
            .type("EXCEL")
            .statusAllegato(StatusAllegato.IN_GENERAZIONE.name())
            .active(true)
            .build();

        tempAllegato.setCreatedBy(codiceFiscale);
        tempAllegato.setCreatedByNameSurname(nomeCompleto);
        tempAllegato.setCreatedByRole(ruoloUtente);
        tempAllegato.setValidity(true);

        log.info("Salvataggio allegato temporaneo Excel batch per idPiaoList={}", idPiaoList);

        // Recupera il messaggio dalla configurazione DB, poi salva allegato e invia notifica
        return configurazioniService.getConfigurazioneByCodice(KeyNotification.NOTIFICA_GENERA_EXCEL.name())
            .onErrorResume(e -> {
                log.warn("Errore recupero configurazione {}: {}", KeyNotification.NOTIFICA_GENERA_EXCEL.name(), e.getMessage());
                return Mono.just(new GenericResponseDTO<>());
            })
            .defaultIfEmpty(new GenericResponseDTO<>())
            .flatMap(configResponse -> {
                if (configResponse.getData() != null && configResponse.getData().getValore() != null) {
                    excelNotification.setMessage(configResponse.getData().getValore());
                    log.info("Messaggio notifica Excel caricato da configurazione: {}", configResponse.getData().getValore());
                } else {
                    log.info("Configurazione {} non trovata, uso messaggio di default", KeyNotification.NOTIFICA_GENERA_EXCEL.name());
                }

                // Salva l'allegato, settalo sulla notification, poi invia
                return allegatoService.saveAllegatoSenzaUpload(tempAllegato)
                    .flatMap(allegatoResponse -> {
                        if (allegatoResponse.getData() != null && allegatoResponse.getData().getId() != null) {
                            AllegatoDTO savedAllegato = allegatoResponse.getData();
                            log.info("Allegato temporaneo Excel batch salvato con id={}", savedAllegato.getId());
                            excelNotification.setAllegato(savedAllegato);
                        } else {
                            log.warn("Allegato temporaneo Excel batch salvato ma senza ID");
                            excelNotification.setAllegato(tempAllegato);
                        }
                        return sendExcelBatchNotification(List.of(excelNotification));
                    })
                    .onErrorResume(e -> {
                        log.error("Errore salvataggio allegato temporaneo Excel batch: {}", e.getMessage(), e);
                        excelNotification.setAllegato(tempAllegato);
                        return sendExcelBatchNotification(List.of(excelNotification));
                    });
            });
    }

    /**
     * Invia la lista di ExcelNotificationDTO al Notifica_BE per la scrittura sulla coda.
     */
    private Mono<GenericResponseDTO<List<ExcelNotificationDTO>>> sendExcelBatchNotification(
            List<ExcelNotificationDTO> excelNotifications) {

        log.info("Invio batch di {} richieste generazione Excel al Notifica_BE", excelNotifications.size());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClientService.post(
            "/api/v1/excel/generation/batch",
            notificaBeType,
            excelNotifications,
            headers,
            new ParameterizedTypeReference<GenericResponseDTO<List<ExcelNotificationDTO>>>() {}
        ).doOnNext(r -> log.info("Richiesta generazione Excel batch inviata con successo"))
         .onErrorResume(e -> {
             log.error("Errore invio richiesta generazione Excel batch: {}", e.getMessage(), e);
             GenericResponseDTO<List<ExcelNotificationDTO>> errResp = new GenericResponseDTO<>();
             errResp.setStatus(new Status());
             errResp.getStatus().setSuccess(Boolean.FALSE);
             errResp.setError(new Error());
             errResp.getError().setMessageError(e.getMessage());
             return Mono.just(errResp);
         });
    }
}

