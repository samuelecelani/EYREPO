package it.ey.piao.bff.service.impl;

import it.ey.dto.*;
import it.ey.dto.Error;
import it.ey.dto.external.IpaAmministrazioneExternalDTO;
import it.ey.dto.external.IpaAmministrazionePublicExternalDTO;
import it.ey.enums.AzioniEmail;
import it.ey.enums.Ruolo;
import it.ey.enums.Sezione;
import it.ey.enums.WebServiceType;
import it.ey.piao.bff.httpClient.WebClientService;
import it.ey.piao.bff.service.ISezione1Service;
import it.ey.piao.bff.util.EmailNotificationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class Sezione1ServiceImpl implements ISezione1Service {

    private final WebClientService webClientService;
    private final WebServiceType webServiceType;
    private final EmailNotificationHelper emailNotificationHelper;
    private static final Logger log = LoggerFactory.getLogger(Sezione1ServiceImpl.class);

    public Sezione1ServiceImpl(WebClientService webClientService,
                               EmailNotificationHelper emailNotificationHelper) {
        this.webClientService = webClientService;
        this.webServiceType = WebServiceType.API;
        this.emailNotificationHelper = emailNotificationHelper;
    }


    @Override
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione1DTO request) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.post("/sezione1/save",webServiceType,request,headers,Void.class)
            .doOnNext(response -> log.info("Sezione1 Salvata/Modficata: {}", response))
            .map(sezione1-> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione1 {}", e);
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }
    @Override
    public Mono<GenericResponseDTO<Sezione1DTO>> findByPiao(Long idPiao,String codiceFiscale) {
        log.info("Richiesta lista di tutte le funzuonalità sulla base del ruolo passato");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get("/sezione1/"+ idPiao ,webServiceType,headers,Sezione1DTO.class)
            .doOnNext(response -> log.info("Sezione recuperata: {}", response))
            // Arricchisce la Sezione1DTO popolando l'AnagraficaDTO:
            // - se codiceFiscale è valorizzato: IPA + DB (merge)
            // - se codiceFiscale è null/blank: solo DB
            .flatMap(sezione1 -> {
                if (sezione1 == null) {
                    return Mono.justOrEmpty(sezione1);
                }

                Mono<AnagraficaDTO> dbMono = (sezione1.getId() != null)
                    ? getAnagraficaFromDb(sezione1.getId())
                    : Mono.empty();

                boolean cfPresente = codiceFiscale != null && !codiceFiscale.isBlank();

                // Caso 1: nessun CF -> solo chiamata DB
                if (!cfPresente) {
                    log.info("CodiceFiscale non fornito: recupero AnagraficaDTO solo da DB per idSezione1={}", sezione1.getId());
                    return dbMono
                        .map(dbAnagrafica -> {
                            sezione1.setAnagrafica(dbAnagrafica);
                            return sezione1;
                        })
                        .defaultIfEmpty(sezione1)
                        .onErrorResume(e -> {
                            log.error("Errore recupero anagrafica DB per idSezione1={}: {}", sezione1.getId(), e.getMessage(), e);
                            return Mono.just(sezione1);
                        });
                }

                // Caso 2: CF presente -> IPA + DB con merge
                Mono<GenericResponseDTO<AnagraficaDTO>> ipaMono = getAnagraficaFromIpa(codiceFiscale);

                return Mono.zip(
                        ipaMono.defaultIfEmpty(new GenericResponseDTO<>()),
                        dbMono.defaultIfEmpty(new AnagraficaDTO())
                    )
                    .map(tuple -> {
                        GenericResponseDTO<AnagraficaDTO> ipaResp = tuple.getT1();
                        AnagraficaDTO dbAnagrafica = tuple.getT2();

                        AnagraficaDTO merged;
                        if (ipaResp.getStatus() != null
                            && Boolean.TRUE.equals(ipaResp.getStatus().isSuccess())
                            && ipaResp.getData() != null) {
                            merged = ipaResp.getData();
                            log.info("AnagraficaDTO valorizzata da IPA per CF={}", codiceFiscale);
                        } else {
                            log.warn("Chiamata IPA non andata a buon fine per CF={}", codiceFiscale);
                            merged = new AnagraficaDTO();
                            merged.setCodiceFiscale(codiceFiscale);
                        }

                        // Merge dei campi presenti solo su DB (non forniti da IPA)
                        if (dbAnagrafica.getId() != null) merged.setId(dbAnagrafica.getId());
                        if (dbAnagrafica.getIdSezione1() != null) merged.setIdSezione1(dbAnagrafica.getIdSezione1());
                        merged.setNomeRPCT(dbAnagrafica.getNomeRPCT());
                        merged.setCognomeRCTP(dbAnagrafica.getCognomeRCTP());
                        merged.setRuoloRPCT(dbAnagrafica.getRuoloRPCT());
                        merged.setDataNominaRPCT(dbAnagrafica.getDataNominaRPCT());
                        merged.setIndirizzoURP(dbAnagrafica.getIndirizzoURP());
                        merged.setTelefono(dbAnagrafica.getTelefono());
                        log.info("AnagraficaDTO arricchita con campi DB per idSezione1={}", sezione1.getId());

                        sezione1.setAnagrafica(merged);
                        return sezione1;
                    })
                    .onErrorResume(e -> {
                        log.error("Errore recupero/merge anagrafica per CF {}: {}", codiceFiscale, e.getMessage(), e);
                        return Mono.just(sezione1);
                    });
            })
            .map(sezione1-> {
                GenericResponseDTO<Sezione1DTO> finalResponse = new GenericResponseDTO<>();
                if (sezione1 == null){
                    finalResponse.setError(new Error());
                    finalResponse.getError().setMessageError("Errore nel recupero Sezione");
                }
                finalResponse.setData(sezione1);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .doOnError(e -> {
                log.error("Errore Salvataggio/modifica Sezione1 {}", e);
                GenericResponseDTO<Sezione1DTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.FALSE);
                finalResponse.setError(new Error());
                finalResponse.getError().setMessageError(e.getMessage());
            });
    }

    /**
     * Recupera l'AnagraficaDTO dal DB (BE) tramite idSezione1.
     * Usata per popolare i campi non forniti da IPA: nomeRPCT, cognomeRCTP,
     * ruoloRPCT, dataNominaRPCT, indirizzoURP, telefono.
     */
    private Mono<AnagraficaDTO> getAnagraficaFromDb(Long idSezione1) {
        log.info("Recupero anagrafica da DB per idSezione1={}", idSezione1);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.get(
                "/anagrafica/by-sezione1/" + idSezione1,
                webServiceType,
                headers,
                AnagraficaDTO.class)
            .doOnNext(r -> log.info("Anagrafica DB ricevuta per idSezione1={}: {}", idSezione1, r))
            .onErrorResume(e -> {
                log.error("Errore recupero anagrafica DB per idSezione1={}: {}", idSezione1, e.getMessage(), e);
                return Mono.empty();
            });
    }

    @Override
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id, String codicePa) {
        log.info("Richiesta validazione stato Sezione1 per id={}, codicePa={}", id, codicePa);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService.patch("/sezione1/validazione/" + id, webServiceType, new Sezione1DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Modifica Stato Sezione1 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_1, AzioniEmail.RICHIEDI_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore Modifica stato Sezione1 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }
    @Override
    public Mono<GenericResponseDTO<Void>> validaSezione(Long id, String codicePa) {
        log.info("Validazione Sezione1 per id={}, codicePa={}", id, codicePa);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione1/valida-sezione/" + id, webServiceType, new Sezione1DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Valida Sezione1 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_1, AzioniEmail.ACCETTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore valida Sezione1 {}", e.getMessage(), e);
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
        log.info("Rifiuto validazione Sezione1 per id={}, codicePa={}", id, codicePa);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        return webClientService
            .patch("/sezione1/rifiuta-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Rifiuta validazione Sezione1 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_1, AzioniEmail.RIFIUTA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore rifiuta validazione Sezione1 {}", e.getMessage(), e);
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
        log.info("Revoca validazione Sezione1 per id={}, codicePa={}", id, codicePa);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione1/revoca-validazione/" + id, webServiceType, osservazioni, headers, Void.class)
            .doOnSuccess(response -> log.info("Revoca validazione Sezione1 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_REFERENTE.name(), Ruolo.ROLE_REDATTORE.name()), Sezione.SEZIONE_1, AzioniEmail.REVOCA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore revoca validazione Sezione1 {}", e.getMessage(), e);
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
        log.info("Annulla validazione Sezione1 per id={}, codicePa={}", id, codicePa);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        return webClientService
            .patch("/sezione1/annulla-validazione/" + id, webServiceType, new Sezione1DTO(), headers, Void.class)
            .doOnSuccess(response -> log.info("Annulla validazione Sezione1 completata per id={}", id))
            .then(emailNotificationHelper.sendEmailToUtentiByRuoli(codicePa,
                List.of(Ruolo.ROLE_VALIDATORE.name(), Ruolo.ROLE_REFERENTE.name()), Sezione.SEZIONE_1, AzioniEmail.ANNULLA_VALIDAZIONE, null))
            .then(Mono.defer(() -> {
                GenericResponseDTO<Void> finalResponse = new GenericResponseDTO<>();
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return Mono.just(finalResponse);
            }))
            .onErrorResume(e -> {
                log.error("Errore annulla validazione Sezione1 {}", e.getMessage(), e);
                GenericResponseDTO<Void> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    @Override
    public Mono<GenericResponseDTO<AnagraficaDTO>> getAnagraficaFromIpa(String codiceFiscale) {
        log.info("Recupero anagrafica da IPA per codice fiscale: {}", codiceFiscale);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1) GET /api/Public/GetAdministrationsInfoForCodiceFiscale?filter={cf} (DFP)
        Mono<IpaAmministrazioneExternalDTO> getCall = webClientService.get(
                "/api/Public/GetAdministrationsInfoForCodiceFiscale?filter=" + codiceFiscale,
                WebServiceType.IPA,
                headers,
                IpaAmministrazioneExternalDTO.class, true)
            .doOnNext(r -> log.info("Risposta IPA GET ricevuta: {}", r))
            .doOnError(e -> log.error("Errore chiamata IPA GET: {}", e.getMessage(), e));

        // 2) POST /api/Public (DFP2) - body con il codice fiscale, ritorna un array
        Map<String, Object> body = new HashMap<>();
        body.put("fiscalCode", codiceFiscale);

        Mono<List<IpaAmministrazionePublicExternalDTO>> postCall = webClientService.post(
                "/api/Public",
                WebServiceType.IPA,
                body,
                headers,
                new ParameterizedTypeReference<List<IpaAmministrazionePublicExternalDTO>>() {}, true)
            .doOnNext(r -> log.info("Risposta IPA POST ricevuta - {} elementi", r != null ? r.size() : 0))
            .doOnError(e -> log.error("Errore chiamata IPA POST: {}", e.getMessage(), e));

        return Mono.zip(getCall, postCall)
            .map(tuple -> {
                IpaAmministrazioneExternalDTO dfpResponse = tuple.getT1();
                List<IpaAmministrazionePublicExternalDTO> dfp2Response = tuple.getT2();
                log.info("DFP response: {}", dfpResponse);
                log.info("DFP2 response: {}", dfp2Response);

                AnagraficaDTO anagrafica = mapToAnagrafica(codiceFiscale, dfpResponse, dfp2Response);

                GenericResponseDTO<AnagraficaDTO> finalResponse = new GenericResponseDTO<>();
                finalResponse.setData(anagrafica);
                finalResponse.setStatus(new Status());
                finalResponse.getStatus().setSuccess(Boolean.TRUE);
                return finalResponse;
            })
            .flatMap(resp -> getCodiceIpaFromSession()
                .doOnNext(codiceIpa -> {
                    if (resp.getData() != null && resp.getData().getCodiceIPA() == null) {
                        resp.getData().setCodiceIPA(codiceIpa);
                        log.info("CodiceIPA valorizzato dalla sessione utente: {}", codiceIpa);
                    }
                })
                .thenReturn(resp)
                .defaultIfEmpty(resp))
            .onErrorResume(e -> {
                log.error("Errore recupero anagrafica IPA per CF {}: {}", codiceFiscale, e.getMessage(), e);
                GenericResponseDTO<AnagraficaDTO> errorResponse = new GenericResponseDTO<>();
                errorResponse.setStatus(new Status());
                errorResponse.getStatus().setSuccess(Boolean.FALSE);
                errorResponse.setError(new Error());
                errorResponse.getError().setMessageError(e.getMessage());
                return Mono.just(errorResponse);
            });
    }

    /**
     * Recupera il codiceIPA (codePA) dalla PA attiva dell'utente in sessione.
     */
    private Mono<String> getCodiceIpaFromSession() {
        return ReactiveSecurityContextHolder.getContext()
            .doOnNext(ctx -> log.info("SecurityContext recuperato: auth={}", ctx.getAuthentication()))
            .flatMap(ctx -> {
                var auth = ctx.getAuthentication();
                if (auth == null) {
                    log.warn("Authentication assente nel SecurityContext");
                    return Mono.empty();
                }
                Object principal = auth.getPrincipal();
                log.info("Principal in sessione: classe={}, value={}",
                    principal != null ? principal.getClass().getName() : "null", principal);
                if (!(principal instanceof UserDTO user)) {
                    log.warn("Principal non è UserDTO");
                    return Mono.empty();
                }
                if (user.getPaRiferimento() == null || user.getPaRiferimento().isEmpty()) {
                    log.warn("paRiferimento assente/vuoto per utente in sessione");
                    return Mono.empty();
                }
                // prima cerco la PA attiva, in fallback prendo la prima con codePA valorizzato
                String codePA = user.getPaRiferimento().stream()
                    .filter(PaRiferimentoDTO::isAttiva)
                    .map(PaRiferimentoDTO::getCodePA)
                    .filter(c -> c != null && !c.isBlank())
                    .findFirst()
                    .orElseGet(() -> user.getPaRiferimento().stream()
                        .map(PaRiferimentoDTO::getCodePA)
                        .filter(c -> c != null && !c.isBlank())
                        .findFirst()
                        .orElse(null));
                log.info("codiceIPA estratto dalla sessione: {}", codePA);
                return Mono.justOrEmpty(codePA);
            })
            .switchIfEmpty(Mono.fromRunnable(() -> log.warn("getCodiceIpaFromSession: nessun codiceIPA trovato (Mono vuoto)")));
    }

    /**
     * Mappatura dati IPA -> AnagraficaDTO secondo la tabella di configurazione:
     * - DFP  (GET /api/Public/GetAdministrationsInfoForCodiceFiscale): tipologiaIstat, acronimo,
     *   sitoIstituzionale, altro(pec), facebook, areeOrganizzativeOmogenee -> codiceAmministrazione (codiceIPA),
     *   UO filtrata per "Ufficio_Transizione_Digitale" -> nome/cognome/descrizione RTD.
     * - DFP2 (POST /api/Public): email(mail), address + city + district (indirizzoSedeLegale),
     *   notStructuredData[source=BDAP] -> PartitaIva.
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
                // codiceIPA = codiceAmministrazione della prima AOO (es. "c_f657")
                anagrafica.setCodiceIPA(aoo.getCodiceAmministrazione());

                // RTD: filtro UO per codiceUo = "Ufficio_Transizione_Digitale"
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
            }else{

            }
        }

        // ---- DFP2 (POST) ----
        if (dfp2Response != null && !dfp2Response.isEmpty()) {
            IpaAmministrazionePublicExternalDTO amm = dfp2Response.get(0);

            anagrafica.setDenominazioneEnte(amm.getName());
            anagrafica.setMail(amm.getEmail());

            // indirizzoSedeLegale = address + ", " + city + " (" + district + ")"
            StringBuilder indirizzo = new StringBuilder();
            if (amm.getAddress() != null && !amm.getAddress().isBlank()) {
                indirizzo.append(amm.getAddress());
            }
            if (amm.getCity() != null && !amm.getCity().isBlank()) {
                if (indirizzo.length() > 0) indirizzo.append(", ");
                indirizzo.append(amm.getCity());
            }
            if (amm.getDistrict() != null && !amm.getDistrict().isBlank()) {
                indirizzo.append(" (").append(amm.getDistrict()).append(")");
            }
            if (indirizzo.length() > 0) {
                anagrafica.setIndirizzoSedeLegale(indirizzo.toString());
            }

            // P.IVA da notStructuredData con source = BDAP
            if (amm.getNotStructuredData() != null) {
                amm.getNotStructuredData().stream()
                    .filter(n -> "BDAP".equalsIgnoreCase(n.getSource()) && n.getNotStructuredData() != null)
                    .findFirst()
                    .ifPresent(n -> {
                        Object piva = n.getNotStructuredData().get("PartitaIva");
                        if (piva != null) {
                            anagrafica.setPiva(piva.toString());
                        }
                    });
            }
        }

        return anagrafica;
    }

}
