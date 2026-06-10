package it.ey.piao.bff.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.enums.AzioniEmail;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.IStorageMinervaService;
import it.ey.piao.bff.util.EmailNotificationHelper;
import it.ey.piao.bff.service.ISezione31Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class Sezione31ServiceImpl implements ISezione31Service
{
    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final WebServiceType webServiceTypeMinerva;
    private final WebServiceType tokenMinerva;
    private final EmailNotificationHelper emailNotificationHelper;
    private final ObjectMapper objectMapper;
    private final IStorageMinervaService storageMinervaService;
    private static final Logger log = LoggerFactory.getLogger(Sezione31ServiceImpl.class);

    @Value("${minerva.mock}")
    private boolean mockMinerva;

    @Value("${minerva.client_id}")
    private String CLIENT_ID_MINERVA;
    @Value("${minerva.client_secret}")
    private String CLIENT_SECRET_MINERVA;
    @Value("${minerva.name_token}")
    private String NAME_TOKEN;

    public Sezione31ServiceImpl(WebClientService webClientService,
                                EmailNotificationHelper emailNotificationHelper,
                                ObjectMapper objectMapper,
                                IStorageMinervaService storageMinervaService)
    {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.webServiceTypeMinerva = WebServiceType.MINERVA;
        this.tokenMinerva = WebServiceType.TOKEN_MINERVA;
        this.emailNotificationHelper = emailNotificationHelper;
        this.objectMapper = objectMapper;
        this.storageMinervaService = storageMinervaService;
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
     * Recupero dei dati del grafico Sezione 31.
     * <ul>
     *   <li>Se {@code minerva.mock=true} restituisce i dati mock.</li>
     *   <li>Altrimenti effettua:
     *     <ol>
     *       <li>chiamata client_credentials per ottenere il token Minerva;</li>
     *       <li>GET {@code /api/internal/v1/profilo-ruolo-dipendenti?codiceIpaAmministrazione={codiceIpa}&limit=100&offset=0}
     *           sul base url Minerva (WebServiceType.MINERVA);</li>
     *       <li>salvataggio dell'intero payload JSON su {@code storageminerva}
     *           (upsert per identitafk + codiceipa + codtipologiafk=SEZIONE_31) - fire-and-forget;</li>
     *       <li>mapping flessibile della risposta in lista di {@link GraficoSezione31DTO}.</li>
     *     </ol>
     *   </li>
     * </ul>
     */
    @Override
    public Mono<GenericResponseDTO<List<GraficoSezione31DTO>>> getGraficoSezione31Mock(Long idEntitaFK, String codiceIpa, Boolean storageMinerva) {
        if (mockMinerva) {
            log.info("Mock chiamata servizio Minerva - getGraficoSezione31Mock");

            List<GraficoSezione31DTO> mockData = Arrays.asList(
                GraficoSezione31DTO.builder().code("x4536").key("ISTRUTTORE TECNICO").value("50").build(),
                GraficoSezione31DTO.builder().code("cvgbd").key("ISTRUTTORE AMMINISTRATIVO").value("30").build(),
                GraficoSezione31DTO.builder().code("lpgh45").key("ESPERTO/A DI ANALISI DATI").value("20").build()
            );

            GenericResponseDTO<List<GraficoSezione31DTO>> response = new GenericResponseDTO<>();
            response.setData(mockData);
            response.setStatus(Status.builder().isSuccess(true).build());

            log.info("Mock Minerva - Restituiti {} elementi per il grafico Sezione 31", mockData.size());
            return Mono.just(response);
        }

        log.info("Chiamata REALE servizio Minerva - getGraficoSezione31 (codiceIpa={}, idEntitaFK={})",
            codiceIpa, idEntitaFK);

        return webClientService.postWithCredential(tokenMinerva, CLIENT_ID_MINERVA, CLIENT_SECRET_MINERVA, true)
            .flatMap(responseToken -> {
                String token = (String) responseToken.get(NAME_TOKEN);

                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");
                headers.set("Authorization", "Bearer " + token);

                String url = "/api/internal/v1/profilo-ruolo-dipendenti?codiceIpaAmministrazione=" + codiceIpa
                    + "&limit=100&offset=0";

                return webClientService.get(url, webServiceTypeMinerva, headers, Object.class, true)
                    .doOnNext(raw -> log.info("Risposta Minerva profilo-ruolo-dipendenti ricevuta"))
                    .map(raw -> {
                        // Salvataggio storageMinerva (fire-and-forget) con l'intero payload SOLO se richiesto
                        if (Boolean.TRUE.equals(storageMinerva)) {
                            saveOnStorageMinerva(raw, codiceIpa, idEntitaFK);
                        } else {
                            log.info("[storageMinerva-Sez31] storageMinerva={} -> skip persistenza", storageMinerva);
                        }

                        // Mapping flessibile -> GraficoSezione31DTO
                        List<GraficoSezione31DTO> data = mapToGraficoSezione31(raw);

                        GenericResponseDTO<List<GraficoSezione31DTO>> response = new GenericResponseDTO<>();
                        response.setData(data);
                        response.setStatus(Status.builder().isSuccess(true).build());
                        log.info("Grafico Sezione31 - mappati {} elementi", data.size());
                        return response;
                    });
            })
            .onErrorResume(e -> {
                log.error("Errore recupero grafico Sezione31 da Minerva: {}", e.getMessage(), e);
                GenericResponseDTO<List<GraficoSezione31DTO>> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(Status.builder().isSuccess(false).build());
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(
                    "Errore recupero grafico Sezione31 da Minerva: " + e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    /**
     * Salvataggio inline su {@code storageminerva}: upsert per chiave logica
     * (identitafk, codiceipa, codtipologiafk=SEZIONE_31) memorizzando come {@code valore}
     * il JSON dell'intero payload Minerva.
     * Fire-and-forget: errori loggati ma non propagati alla response principale.
     */
    private void saveOnStorageMinerva(Object payload, String codiceIpa, Long identitafk) {
        if (identitafk == null) {
            log.warn("[storageMinerva-Sez31] identitafk null: skip persistenza");
            return;
        }
        if (payload == null) {
            log.warn("[storageMinerva-Sez31] payload null: skip persistenza");
            return;
        }
        String codtipologia = Sezione.SEZIONE_31.toString();
        String valoreJson;
        try {
            valoreJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("[storageMinerva-Sez31] Errore serializzazione JSON: {}", e.getMessage(), e);
            return;
        }

        log.info("[storageMinerva-Sez31] Persistenza: identitafk={}, codiceipa={}, codtipologiafk={}",
            identitafk, codiceIpa, codtipologia);

        StorageMinervaDTO dto = StorageMinervaDTO.builder()
            .identitafk(identitafk)
            .codtipologiafk(codtipologia)
            .valore(valoreJson)
            .codiceipa(codiceIpa)
            .build();

        storageMinervaService.saveOrUpdate(dto)
            .doOnNext(saved -> log.info("[storageMinerva-Sez31] Persistito: id={}",
                saved != null ? saved.getId() : null))
            .doOnError(e -> log.error("[storageMinerva-Sez31] Errore persistenza: {}", e.getMessage(), e))
            .onErrorResume(e -> Mono.empty())
            .subscribe();
    }

    /**
     * Mapping della risposta Minerva {@code /api/internal/v1/profilo-ruolo-dipendenti}
     * in lista di {@link GraficoSezione31DTO}.
     * <p>Struttura attesa:
     * <pre>
     * {
     *   "numeroTotaleProfili": 52,
     *   "profili": [
     *     { "codiceProfiloRuolo": "...", "titoloProfiloRuolo": "...", "numeroDipendenti": 2 },
     *     ...
     *   ]
     * }
     * </pre>
     * Mapping:
     * <ul>
     *   <li>code  &lt;- codiceProfiloRuolo</li>
     *   <li>key   &lt;- titoloProfiloRuolo</li>
     *   <li>value &lt;- numeroDipendenti (come stringa)</li>
     * </ul>
     */
    private List<GraficoSezione31DTO> mapToGraficoSezione31(Object raw) {
        List<GraficoSezione31DTO> out = new ArrayList<>();
        if (raw == null) return out;

        JsonNode root = objectMapper.valueToTree(raw);
        JsonNode profili = root.get("profili");
        if (profili == null || !profili.isArray()) {
            log.warn("[mapGraficoSez31] Campo 'profili' assente o non array nel payload Minerva");
            return out;
        }

        for (JsonNode el : profili) {
            String code = textOrNull(el, "codiceProfiloRuolo");
            String key = textOrNull(el, "titoloProfiloRuolo");
            JsonNode num = el.get("numeroDipendenti");
            String value = (num != null && !num.isNull()) ? num.asText() : null;

            out.add(GraficoSezione31DTO.builder()
                .code(code)
                .key(key)
                .value(value)
                .build());
        }

        if (log.isInfoEnabled() && root.has("numeroTotaleProfili")) {
            log.info("[mapGraficoSez31] numeroTotaleProfili={}, profili mappati={}",
                root.get("numeroTotaleProfili").asInt(), out.size());
        }
        return out;
    }

    private String textOrNull(JsonNode el, String field) {
        if (el == null) return null;
        JsonNode v = el.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank()) ? null : s;
    }
}
