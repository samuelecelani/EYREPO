package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.*;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import it.ey.piao.bff.service.ISezione331Service;
import it.ey.piao.bff.service.IStorageMinervaService;
import it.ey.utils.DynamicTableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class Sezione331ServiceImpl implements ISezione331Service {
    private final WebServiceType tokenMinerva;
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private final WebServiceType webServiceTypeMinerva;
    private final ObjectMapper objectMapper;
    private final IStorageMinervaService storageMinervaService;
    private static final Logger log = LoggerFactory.getLogger(Sezione331ServiceImpl.class);

    @Value("${minerva.client_id}")
    private String CLIENT_ID_MINERVA;
    @Value("${minerva.client_secret}")
    private String CLIENT_SECRET_MINERVA;
    @Value("${minerva.name_token}")
    private String NAME_TOKEN;
    @Value("${minerva.mock}")
    private boolean MOCK_MINERVA;

    public Sezione331ServiceImpl(WebClientService webClientService, ObjectMapper objectMapper,
                                  EmailNotificationHelper emailNotificationHelper,
                                  IStorageMinervaService storageMinervaService) {
        this.webClientService = webClientService;
        this.webServiceTypeMinerva = WebServiceType.MINERVA;
        this.webServiceType = WebServiceType.API;
        this.tokenMinerva = WebServiceType.TOKEN_MINERVA;
        this.objectMapper = objectMapper;
        this.emailNotificationHelper = emailNotificationHelper;
        this.storageMinervaService = storageMinervaService;
    }


    @Override
    public Mono<GenericResponseDTO<Sezione331DTO>> getOrCreate(PiaoDTO request) {
        log.info("Ricerca Sezione331 per idPiao: {}", request);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String url = "/sezione331/piao";
        return webClientService.post(url, webServiceType, request, headers, Sezione331DTO.class)
            .doOnNext(response -> log.info("Sezione331 trovata per idPiao {}: {}", request, response))
            .map(sezione331 -> {
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(sezione331);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .onErrorResume(e -> {
                log.error("Errore nella ricerca di Sezione331 per idPiao {}: {}", request, e.getMessage());
                GenericResponseDTO<Sezione331DTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    @Override
    public Mono<GenericResponseDTO<Sezione331DTO>> saveOrUpdate(Sezione331DTO request) {
        log.info("Richiesta salvataggio/aggiornamento Sezione331");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione331/save", webServiceType, request, headers, Sezione331DTO.class)
            .doOnNext(response -> log.info("Sezione331 Salvata/Modificata: {}", response))
            .map(sezione331 -> {
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione331 == null) {
                    finalResponse.setError(new it.ey.dto.Error());
                    finalResponse.getError().setMessageError("Errore nel salvataggio/modifica");
                }
                finalResponse.setData(sezione331);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione331 {}", e);
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa) {
        log.info("Richiesta validazione stato Sezione331 per id={}", id);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione331/validazione/" + id, webServiceType, new Sezione331DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Modifica Stato Sezione331 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_331, AzioniEmail.RICHIEDI_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione331 {}", e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Sezione331DTO>> findByPiao(Long idPiao) {
        log.info("Ricerca Sezione331 per idPiao: {}", idPiao);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione331/" + idPiao, webServiceType, headers, Sezione331DTO.class)
            .doOnNext(response -> log.info("Errore nel recupero Sezione: {}", response))
            .map(sezione331 -> {
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione331 == null) {
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione");
                }
                finalResponse.setData(sezione331);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione331 {}", e);
                GenericResponseDTO<Sezione331DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa) {
        log.info("Validazione Sezione331 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione331/valida-sezione/" + id, webServiceType, new Sezione331DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Valida Sezione331 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_331, AzioniEmail.ACCETTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore valida Sezione331 {}", e.getMessage(), e);
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
        log.info("Rifiuto validazione Sezione331 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione331/rifiuta-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Rifiuta validazione Sezione331 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_331, AzioniEmail.RIFIUTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione Sezione331 {}", e.getMessage(), e);
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
        log.info("Revoca validazione Sezione331 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione331/revoca-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Revoca validazione Sezione331 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_331, AzioniEmail.REVOCA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore revoca validazione Sezione331 {}", e.getMessage(), e);
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
        log.info("Annullo validazione Sezione331 per id={}", id);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione331/annulla-validazione/" + id, webServiceType, new Sezione331DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Annulla validazione Sezione331 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_331, AzioniEmail.ANNULLA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore annulla validazione Sezione331 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<Map<String, Object>>> getTabellaMock(TipoTabellaSezione331 tipoTabella) {
        log.info("Recupero tabella  Sezione331 per tipo: {}", tipoTabella);

        String mockJson = getMockJsonByTipo(tipoTabella);

        try {
            JsonNode jsonNode = objectMapper.readTree(mockJson);

            Map<String, Object> table = DynamicTableUtils.buildTableFromJson(jsonNode);

            GenericResponseDTO<Map<String, Object>> response = new GenericResponseDTO<>();
            response.setStatus(Status.builder().isSuccess(true).build());
            response.setData(table);

            log.info("Tabella  Sezione331 [{}] generata con successo", tipoTabella);
            return Mono.just(response);

        } catch (Exception e) {
            log.error("Errore generazione tabella  Sezione331 [{}]: {}", tipoTabella, e.getMessage(), e);

            GenericResponseDTO<Map<String, Object>> errorResponse = new GenericResponseDTO<>();
            errorResponse.setStatus(Status.builder().isSuccess(false).build());
            errorResponse.setError(new Error());
            errorResponse.getError().setMessageError("Errore durante la generazione della tabella: " + e.getMessage());
            return Mono.just(errorResponse);
        }
    }

    @Override
    public Mono<GenericResponseDTO<List<Map<String, Object>>>> getAllTabelleMock(String codiceAmministrazione,
                                                                                 String annoRiferimento,
                                                                                 Long identitafk,
                                                                                 Boolean storageMinerva) {
        log.info("Recupero tutte le tabelle  Sezione331 (storageMinerva={}, identitafk={})", storageMinerva, identitafk);
        List<Map<String, Object>> allTables = new ArrayList<>();
        GenericResponseDTO<List<Map<String, Object>>> response = new GenericResponseDTO<>();

        try {
            if (MOCK_MINERVA) {
                for (TipoTabellaSezione331 tipo : TipoTabellaSezione331.values()) {
                    String mockJson = getMockJsonByTipo(tipo);
                    JsonNode jsonNode = objectMapper.readTree(mockJson);
                    Map<String, Object> table = DynamicTableUtils.buildTableFromJson(jsonNode);
                    table.put("tipoTabella", tipo.name());
                    table.put("descrizione", tipo.getDescrizione());
                    allTables.add(table);

                }
                response.setStatus(Status.builder().isSuccess(true).build());
                response.setData(allTables);

                log.info("Tutte le tabelle  Sezione331 generate con successo: {} tabelle", allTables.size());
                return Mono.just(response);
            } else {
                return webClientService.postWithCredential(tokenMinerva, CLIENT_ID_MINERVA, CLIENT_SECRET_MINERVA, true)
                    .flatMap(responseToken -> {
                        String token = (String) responseToken.get(NAME_TOKEN);

                        HttpHeaders headers = new HttpHeaders();
                        headers.set("Accept", "application/json");
                        headers.set("Authorization", "Bearer " + token);

                        StringBuilder url = new StringBuilder("/api/internal/v1/ptfa/reports");

                        if (codiceAmministrazione != null && annoRiferimento != null) {
                            url.append("?codiceAmministrazione=").append(codiceAmministrazione)
                                .append("&annoRiferimento=").append(annoRiferimento);
                        }

                        return webClientService.get(url.toString(),webServiceTypeMinerva, headers, Object.class, true)
                            .doOnNext(responseMinerva -> log.info("Tabelle  Sezione331 recuperate da Minerva: {}", responseMinerva))
                            .map(tables -> {
                                try {

                                    String jsonString = objectMapper.writeValueAsString(tables);
                                    JsonNode root = objectMapper.readTree(jsonString);

                                    response.setStatus(Status.builder().isSuccess(true).build());
                                    response.setData(extractMultipleTables(root, codiceAmministrazione, identitafk, storageMinerva));
                                    log.info("Tutte le tabelle  Sezione331 generate con successo: {} tabelle", allTables.size());

                                } catch (JsonProcessingException e) {
                                    throw new RuntimeException(e);
                                }
                                return response;
                            });
                    })
                    .onErrorResume(e -> {
                        log.error("Errore generazione di tutte le tabelle  Sezione331: {}", e.getMessage(), e);
                        GenericResponseDTO<List<Map<String, Object>>> errorResponse = new GenericResponseDTO<>();
                        errorResponse.setStatus(Status.builder().isSuccess(false).build());
                        errorResponse.setError(new Error());
                        errorResponse.getError().setMessageError("Errore durante la generazione delle tabelle: " + e.getMessage());
                        return Mono.just(errorResponse);
                    });
            }

        } catch (Exception e) {
            log.error("Errore generazione di tutte le tabelle  Sezione331: {}", e.getMessage(), e);

            GenericResponseDTO<List<Map<String, Object>>> errorResponse = new GenericResponseDTO<>();
            errorResponse.setStatus(Status.builder().isSuccess(false).build());
            errorResponse.setError(new Error());
            errorResponse.getError().setMessageError("Errore durante la generazione delle tabelle: " + e.getMessage());
            return Mono.just(errorResponse);
        }
    }

    @Override
    public Mono<GenericResponseDTO<String>> getTokenMinerva() {
        return webClientService.postWithCredential(tokenMinerva, CLIENT_ID_MINERVA, CLIENT_SECRET_MINERVA, true)
            .map(responseToken -> {
                String token = (String) responseToken.get(NAME_TOKEN);

                GenericResponseDTO<String> response = new GenericResponseDTO<>();
                response.setData(token);
                response.setStatus(Status.builder().isSuccess(true).build());

                return response;
            })
            .onErrorResume(e -> {
                log.error("Errore recupero token Minerva Sezione331: {}", e.getMessage(), e);

                GenericResponseDTO<String> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(Status.builder().isSuccess(false).build());
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError("Errore recupero token Minerva Sezione331: " + e.getMessage());
                return Mono.just(errorResponse);
            });
    }


    private List<Map<String, Object>> extractMultipleTables(JsonNode root,
                                                            String codiceAmministrazione,
                                                            Long identitafk,
                                                            Boolean storageMinerva) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<Map<String, Object>> tableObject = new ArrayList<>();

        for(String path : DynamicTableUtils.paths){
            tableObject.add(DynamicTableUtils.extractTableFromMinerva(root, path, objectMapper));
        }

        var reportType = root.get("reportType").asText();
        var type = "";

        //switchByReportType  ReportAmmCentAut === PAC || ReportEELL === PAL || ReportUni === UNI

        switch(reportType){
            case "ReportAmmCentAut":
                for (TipoTabellaSezione331 nameTable : DynamicTableUtils.nameTablePAC) {
                    results.add(DynamicTableUtils.createTablePACMinerva(nameTable,tableObject));
                }
                type = "PAC";
                break;
            case "ReportEELL":
                for (TipoTabellaSezione331 nameTable : DynamicTableUtils.nameTablePAL) {
                    results.add(DynamicTableUtils.createTablePALMinerva(nameTable,tableObject));
                }
                type = "PAL";
                break;
            case "ReportUni":
                type = "UNI";
                break;
        }

        // Persistenza inline su storageminerva (fire-and-forget): non blocca la response.
        saveOnStorageMinerva(results, codiceAmministrazione, identitafk, storageMinerva);

        results.add(Map.of("reportType", type));


        return results;
    }

    /**
     * Salvataggio inline su storageminerva.
     * Esegue l'upsert per chiave logica (identitafk, codiceipa, codtipologiafk="SEZIONE_331")
     * salvando come {@code valore} il JSON dell'intera lista di tabelle.
     * <p>
     * Modalità fire-and-forget: l'eventuale errore viene loggato ma NON propagato (la response
     * principale non deve essere bloccata dallo storage).
     */
    private void saveOnStorageMinerva(List<Map<String, Object>> tables,
                                      String codiceAmministrazione,
                                      Long identitafk,
                                      Boolean storageMinerva) {
        if (!Boolean.TRUE.equals(storageMinerva)) {
            log.debug("storageMinerva non richiesto: skip persistenza");
            return;
        }
        if (identitafk == null) {
            log.warn("storageMinerva=true ma identitafk è null: skip persistenza");
            return;
        }
        if (tables == null || tables.isEmpty()) {
            log.warn("Nessuna tabella da persistere: skip");
            return;
        }

        String codtipologia = Sezione.SEZIONE_331.toString();
        String valoreJson;
        try {
            valoreJson = objectMapper.writeValueAsString(tables);
        } catch (JsonProcessingException e) {
            log.error("Errore serializzazione JSON tabelle ({}): {}", codtipologia, e.getMessage(), e);
            return;
        }

        log.info("Persistenza storageminerva: identitafk={}, codiceipa={}, codtipologiafk={}, tabelle={}",
            identitafk, codiceAmministrazione, codtipologia, tables.size());

        StorageMinervaDTO dto = StorageMinervaDTO.builder()
            .identitafk(identitafk)
            .codtipologiafk(codtipologia)
            .valore(valoreJson)
            .codiceipa(codiceAmministrazione)
            .build();

        storageMinervaService.saveOrUpdate(dto)
            .doOnNext(saved -> log.info("StorageMinerva persistito: id={}, codtipologiafk={}",
                saved != null ? saved.getId() : null, codtipologia))
            .doOnError(e -> log.error("Errore persistenza StorageMinerva: {}", e.getMessage(), e))
            .onErrorResume(e -> Mono.empty())
            .subscribe();
    }


    private String getMockJsonByTipo(TipoTabellaSezione331 tipoTabella) {
        return switch (tipoTabella) {
            case CESSAZIONI_ANNO_CORRENTE -> """
                [
                  {"Area contrattuale CCNL e qualifiche":"Dirigente I fascia","Totale unità anno t":2,"Totale risorse da cessazioni anno t":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti I fascia","Totale unità anno t":2,"Totale risorse da cessazioni anno t":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti II fascia + Aree","Totale unità anno t":0,"Totale risorse da cessazioni anno t":0},
                  {"Area contrattuale CCNL e qualifiche":"Totale complessivo","Totale unità anno t":2,"Totale risorse da cessazioni anno t":2}
                ]
                """;
            case VALORE_FINANZIARIO_DOTAZIONE_ORGANICA -> """
                [
                  {"Area contrattuale CCNL e qualifiche":"Dirigente I fascia","Totale unità in D.O.":2,"Valore finanziario della D.O.":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti I fascia","Totale unità in D.O.":2,"Valore finanziario della D.O.":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti II fascia + Aree","Totale unità in D.O.":0,"Valore finanziario della D.O.":0},
                  {"Area contrattuale CCNL e qualifiche":"Totale complessivo","Totale unità in D.O.":2,"Valore finanziario della D.O.":2}
                ]
                """;
            case CONSISTENZA_PERSONALE_DIRIGENZIALE -> """
                [
                  {"Qualifiche":"Dirigente I fascia","Posti in Dotazione Organica":2,"Pers. di ruolo al 31/12/2024 anno t-1":2,"Comandati OUT al 31/12/2024 anno t-1":2,"Totale unità art. 19 comma 5-bis":2,"Totale unità art. 19 comma 6":2,"Totale unità":2},
                  {"Qualifiche":"Totale di cui Dirigenti I fascia","Posti in Dotazione Organica":2,"Pers. di ruolo al 31/12/2024 anno t-1":2,"Comandati OUT al 31/12/2024 anno t-1":2,"Totale unità art. 19 comma 5-bis":2,"Totale unità art. 19 comma 6":2,"Totale unità":2},
                  {"Qualifiche":"Totale di cui Dirigenti II fascia + Aree","Posti in Dotazione Organica":0,"Pers. di ruolo al 31/12/2024 anno t-1":0,"Comandati OUT al 31/12/2024 anno t-1":0,"Totale unità art. 19 comma 5-bis":0,"Totale unità art. 19 comma 6":0,"Totale unità":0},
                  {"Qualifiche":"Totale complessivo","Posti in Dotazione Organica":2,"Pers. di ruolo al 31/12/2024 anno t-1":2,"Comandati OUT al 31/12/2024 anno t-1":2,"Totale unità art. 19 comma 5-bis":2,"Totale unità art. 19 comma 6":2,"Totale unità":2}
                ]
                """;
            case CONSISTENZA_PERSONALE_NON_DIRIGENZIALE -> """
                [
                  {"Area contrattuale CCNL":"Area EP","Posti in Dotazione Organica":2,"Pers. di ruolo al 31/12/2024 anno t-1":3,"Comandati OUT al 31/12/2024 anno t-1":1,"Comandati IN al 31/12/2024 anno t-1":2,"Totale unità":4},
                  {"Area contrattuale CCNL":"Totale di cui Dirigenti I fascia","Posti in Dotazione Organica":2,"Pers. di ruolo al 31/12/2024 anno t-1":3,"Comandati OUT al 31/12/2024 anno t-1":1,"Comandati IN al 31/12/2024 anno t-1":2,"Totale unità":4}
                ]
                """;
            case RIEPILOGO_ASSUNZIONI_DIRIGENZIALE -> """
                [
                  {"Qualifiche":"Dirigenti I fascia","Tipologia di reclutamento":"Concorso pubblico","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Qualifiche":"Dirigenti II fascia","Tipologia di reclutamento":"Corso concorso SNA","Fonte di finanziamento":"xxx","Totale unità":1},
                  {"Qualifiche":"Totale dirigenti I fascia","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3},
                  {"Qualifiche":"Totale dirigenti II fascia","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":1}
                ]
                """;
            case RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI -> """
                [
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"Stabilizzazioni","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3},
                  {"Area contrattuale CCNL":"Totale aree","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3},
                  {"Area contrattuale CCNL":"Totale complessivo assunzioni","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":3}
                ]
                """;
            case RIEPILOGO_CESSAZIONI -> """
                [
                  {"Area contrattuale CCNL e qualifiche":"Dirigente I fascia","Totale unità anno t-1":2,"Totale risorse da cessazioni anno t-1":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti I fascia","Totale unità anno t-1":2,"Totale risorse da cessazioni anno t-1":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti II fascia + Aree","Totale unità anno t-1":0,"Totale risorse da cessazioni anno t-1":0},
                  {"Area contrattuale CCNL e qualifiche":"Totale complessivo","Totale unità anno t-1":2,"Totale risorse da cessazioni anno t-1":2}
                ]
                """;
            case DOTAZIONE_ORGANICA_RIMODULAZIONE -> """
                [
                  {"Area contrattuale CCNL e qualifiche":"Dirigente I fascia","Totale unità in D.O.":2,"Valore finanziario della D.O.":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti I fascia","Totale unità in D.O.":2,"Valore finanziario della D.O.":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti II fascia + Aree","Totale unità in D.O.":0,"Valore finanziario della D.O.":0},
                  {"Area contrattuale CCNL e qualifiche":"Totale complessivo","Totale unità in D.O.":2,"Valore finanziario della D.O.":2}
                ]
                """;
            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE -> """
                [
                  {"Qualifiche":"Dirigente I fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":40},
                  {"Qualifiche":"Totale dirigenti I fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":40},
                  {"Qualifiche":"Totale dirigenti II fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":0}
                ]
                """;
            case COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI -> """
                [
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"Stabilizzazioni","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale aree","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3}
                ]
                """;
            case COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE -> """
                [
                  {"Qualifiche":"Dirigente I fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":40},
                  {"Qualifiche":"Totale dirigenti I fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":40},
                  {"Qualifiche":"Totale dirigenti II fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":0}
                ]
                """;
            case COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI -> """
                [
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"Stabilizzazioni","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale aree","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3}
                ]
                """;
            case CESSAZIONI_SERVIZIO -> """
                [
                  {"Area contrattuale CCNL e qualifiche":"Dirigente I fascia","Totale unità anno t+1":2,"Totale risorse da cessazioni anno t+1":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti I fascia","Totale unità anno t+1":2,"Totale risorse da cessazioni anno t+1":2},
                  {"Area contrattuale CCNL e qualifiche":"Totale di cui Dirigenti II fascia + Aree","Totale unità anno t+1":0,"Totale risorse da cessazioni anno t+1":0},
                  {"Area contrattuale CCNL e qualifiche":"Totale complessivo","Totale unità anno t+1":2,"Totale risorse da cessazioni anno t+1":2}
                ]
                """;
            case COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE -> """
                [
                  {"Qualifiche":"Dirigente I fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":40},
                  {"Qualifiche":"Totale dirigenti I fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":40},
                  {"Qualifiche":"Totale dirigenti II fascia","Tipologia di reclutamento":2,"Fonte di finanziamento":3,"Totale unità":1,"Totale Oneri assunzionali":0}
                ]
                """;
            case COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI -> """
                [
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"Stabilizzazioni","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"xxx","Obiettivo Operativo":"xxx","Profilo professionale":"xxx","Tipologia di reclutamento":"xxx","Fonte di finanziamento":"xxx","Totale unità":"xxx","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3},
                  {"Area contrattuale CCNL":"Totale aree","Obiettivo Operativo":"","Profilo professionale":"","Tipologia di reclutamento":"","Fonte di finanziamento":"","Totale unità":"","Totale oneri assunzionali":3}
                ]
                """;
            default -> "[]";
        };
    }
}
