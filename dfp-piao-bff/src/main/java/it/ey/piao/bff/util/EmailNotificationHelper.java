package it.ey.piao.bff.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.ey.dto.*;
import it.ey.enums.AzioniEmail;
import it.ey.enums.KeyNotification;
import it.ey.enums.Sezione;
import it.ey.piao.bff.service.IConfigurazioniService;
import it.ey.piao.bff.service.INotificationService;
import it.ey.piao.bff.service.IPiaoService;
import it.ey.piao.bff.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * Utility centralizzata per l'invio di email di notifica
 * ai ruoli indicati (validatori, referenti, redattori, ecc.)
 * per qualsiasi sezione del PIAO.
 */
@Component
public class EmailNotificationHelper {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationHelper.class);

    private final IUserService userService;
    private final INotificationService notificationService;
    private final IConfigurazioniService configurazioniService;
    private final IPiaoService piaoService;
    private final ObjectMapper objectMapper;

    public EmailNotificationHelper(IUserService userService,
                                   INotificationService notificationService,
                                   IConfigurazioniService configurazioniService,
                                   IPiaoService piaoService,
                                   ObjectMapper objectMapper) {
        this.userService = userService;
        this.notificationService = notificationService;
        this.configurazioniService = configurazioniService;
        this.piaoService = piaoService;
        this.objectMapper = objectMapper;
    }

    /**
     * Invia un'email a tutti gli utenti della PA che hanno i ruoli specificati
     * nella sezione indicata.
     * Carica MAIL_OBJECT, BODY_EMAIL, FROM_ADDRESS, FROM_ADDRESS_NAME dalla tabella configurazioni,
     * con chiave dinamica basata sulla sezione e azione
     * (es. MAIL_OBJECT_SEZIONE_1_RICHIEDI_VALIDAZIONE, BODY_EMAIL_SEZIONE_1_RICHIEDI_VALIDAZIONE).
     * Scarica gli utenti con i ruoli indicati filtrati per la sezione e invia un'email a ciascuno.
     * <p>
     * Validazione parametri:
     * - Se {@code key} è valorizzata, viene usata direttamente come chiave per MAIL_OBJECT e BODY_EMAIL
     *   (es. MAIL_OBJECT_{key}, BODY_EMAIL_{key}).
     * - Se {@code key} è null, allora sia {@code sezione} che {@code azione} devono essere non-null
     *   e la chiave viene costruita da sezione+azione.
     * - Non è ammesso che key e la coppia sezione/azione siano tutti valorizzati contemporaneamente.
     *
     * @param codicePa   codice PA per recuperare gli utenti destinatari
     * @param roleNames  lista dei ruoli destinatari (es. ROLE_VALIDATORE, ROLE_REFERENTE)
     * @param sezione    enum Sezione da cui si ricava il numero sezione per filtrare gli utenti (nullable se key è valorizzata)
     * @param azione     enum AzioniEmail per identificare il tipo di azione (nullable se key è valorizzata)
     * @param key        chiave diretta per recuperare MAIL_OBJECT e BODY_EMAIL dalla configurazione (nullable se sezione+azione sono valorizzati)
     * @return Mono<Void> che completa dopo l'invio di tutte le email
     */
    public Mono<Void> sendEmailToUtentiByRuoli(String codicePa,
                                                List<String> roleNames, Sezione sezione,
                                                AzioniEmail azione, String key) {

        // Validazione: key != null OPPURE (sezione != null E azione != null), non entrambi
        if (key != null && (sezione != null || azione != null)) {
            log.error("Parametri incompatibili: key e sezione/azione non possono essere tutti valorizzati contemporaneamente");
            return Mono.error(new IllegalArgumentException(
                "Parametri incompatibili: specificare key OPPURE la coppia sezione/azione, non entrambi"));
        }
        if (key == null && (sezione == null || azione == null)) {
            log.error("Parametri insufficienti: se key è null, sezione e azione devono essere entrambi non-null");
            return Mono.error(new IllegalArgumentException(
                "Parametri insufficienti: specificare key OPPURE la coppia sezione/azione"));
        }

        // Determina le chiavi per MAIL_OBJECT e BODY_EMAIL
        String mailObjectKey;
        String bodyKey;
        String sezioneLabel;

        if (key != null) {
            mailObjectKey = "MAIL_OBJECT_" + key;
            bodyKey = "BODY_EMAIL_" + key;
            sezioneLabel = key;
        } else {
            mailObjectKey = "MAIL_OBJECT_" + sezione.name() + "_" + azione.name();
            bodyKey = "BODY_EMAIL_" + sezione.name() + "_" + azione.name();
            sezioneLabel = "Sezione " + resolveNumeroSezioneLabel(sezione);
        }

        log.info("Invio email per {} a ruoli {} per codicePa={}, mailObjectKey='{}', bodyKey='{}'",
            sezioneLabel, roleNames, codicePa, mailObjectKey, bodyKey);

        // 1) Carica le 4 configurazioni email in parallelo
        Mono<String> mailObjectMono = loadConfigValue(mailObjectKey, sezioneLabel);
        Mono<String> bodyMono = loadConfigValue(bodyKey, "");
        Mono<String> fromAddressMono = loadConfigValue(KeyNotification.FROM_ADDRESS.name(), "noreply@piao.gov.it");
        Mono<String> fromNameMono = loadConfigValue(KeyNotification.FROM_ADDRESS_NAME.name(), "PIAO");

        // 2) Scarica gli utenti della PA con i ruoli indicati, filtrati per la sezione (se presente)
        List<String> sezioniFilter = sezione != null ? List.of(resolveNumeroSezione(sezione)) : List.of();
        Mono<GenericResponseDTO<List<UtenteRuoloPaDTO>>> utentiMono =
            userService.findUtentiByRuoloAndSezioni(codicePa, roleNames, sezioniFilter);

        return Mono.zip(mailObjectMono, bodyMono, fromAddressMono, fromNameMono, utentiMono)
            .flatMap(tuple -> {
                String mailObject = tuple.getT1();
                String body = tuple.getT2();
                String fromAddress = tuple.getT3();
                String fromName = tuple.getT4();
                GenericResponseDTO<List<UtenteRuoloPaDTO>> utentiResponse = tuple.getT5();

                if (utentiResponse.getData() == null || utentiResponse.getData().isEmpty()) {
                    log.warn("{}: Nessun utente con ruoli {} trovato per codicePa={}",
                        sezioneLabel, roleNames, codicePa);
                    return Mono.empty();
                }

                MailAddressDTO from = MailAddressDTO.builder()
                    .email(fromAddress)
                    .name(fromName)
                    .build();

                List<UtenteRuoloPaDTO> utenti = utentiResponse.getData();
                log.info("{}: Trovati {} utenti destinatari email per codicePa={}",
                    sezioneLabel, utenti.size(), codicePa);

                // 3) Per ogni utente costruisci e invia l'email
                return Flux.fromIterable(utenti)
                    .filter(utente -> utente.getEmail() != null && !utente.getEmail().isBlank())
                    .flatMap(utente -> {
                        String nomeCompleto = ((utente.getNome() != null ? utente.getNome() : "") + " " +
                                               (utente.getCognome() != null ? utente.getCognome() : "")).trim();

                        MailAddressDTO toAddress = MailAddressDTO.builder()
                            .email(utente.getEmail())
                            .name(nomeCompleto)
                            .build();

                        EmailTaskMessageDTO emailTask = EmailTaskMessageDTO.builder()
                            .fromAddress(from)
                            .toAddresses(List.of(toAddress))
                            .mailObject(mailObject)
                            .mailBody(body)
                            .htmlContent(true)
                            .idModulo("PIAO")
                            .nomeModulo("PIAO")
                            .notificationCodiceFiscale(utente.getCodiceFiscale())
                            .notificationCodicePa(codicePa)
                            .notificationMessage(mailObject)
                            .notificationSender(fromName)
                            .build();

                        log.info("{}: Invio email a {} <{}> per codicePa={}",
                            sezioneLabel, nomeCompleto, utente.getEmail(), codicePa);
                        return notificationService.sendEmail(emailTask)
                            .doOnNext(r -> log.info("{}: Email inviata a {}", sezioneLabel, utente.getEmail()))
                            .onErrorResume(e -> {
                                log.error("{}: Errore invio email a {}: {}",
                                    sezioneLabel, utente.getEmail(), e.getMessage());
                                return Mono.empty();
                            });
                    })
                    .then();
            })
            .onErrorResume(e -> {
                log.error("{}: Errore nell'invio email per codicePa={}: {}",
                    sezioneLabel, codicePa, e.getMessage(), e);
                return Mono.empty();
            });
    }

    /**
     * Invia un'email direttamente all'indirizzo specificato.
     * A differenza di sendEmailToUtentiByRuoli, non scarica gli utenti per ruolo
     * ma usa direttamente l'email passata. L'oggetto e il body dell'email vengono
     * recuperati dalla tabella configurazioni tramite chiavi costruite con l'azione
     * (es. MAIL_OBJECT_APPROVA_PIAO, BODY_EMAIL_APPROVA_PIAO).
     *
     * @param toEmail indirizzo email del destinatario
     * @param azione  enum AzioniEmail per identificare il tipo di azione
     * @return Mono<Void> che completa dopo l'invio dell'email
     */
    public Mono<Void> sendEmailToAddress(String toEmail, AzioniEmail azione) {
        String mailObjectKey = "MAIL_OBJECT_" + azione.name();
        String mailBodyKey = "BODY_EMAIL_" + azione.name();

        log.info("Invio email a {}, mailObjectKey='{}', mailBodyKey='{}'", toEmail, mailObjectKey, mailBodyKey);

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Indirizzo email destinatario mancante, email non inviata");
            return Mono.empty();
        }

        Mono<String> mailObjectMono = loadConfigValue(mailObjectKey, "");
        Mono<String> mailBodyMono = loadConfigValue(mailBodyKey, "");
        Mono<String> fromAddressMono = loadConfigValue(KeyNotification.FROM_ADDRESS.name(), "noreply@piao.gov.it");
        Mono<String> fromNameMono = loadConfigValue(KeyNotification.FROM_ADDRESS_NAME.name(), "PIAO");

        return Mono.zip(mailObjectMono, mailBodyMono, fromAddressMono, fromNameMono)
            .flatMap(tuple -> {
                String mailObject = tuple.getT1();
                String mailBody = tuple.getT2();
                String fromAddress = tuple.getT3();
                String fromName = tuple.getT4();

                MailAddressDTO from = MailAddressDTO.builder()
                    .email(fromAddress)
                    .name(fromName)
                    .build();

                MailAddressDTO toAddress = MailAddressDTO.builder()
                    .email(toEmail)
                    .name(toEmail)
                    .build();

                EmailTaskMessageDTO emailTask = EmailTaskMessageDTO.builder()
                    .fromAddress(from)
                    .toAddresses(List.of(toAddress))
                    .mailObject(mailObject)
                    .mailBody(mailBody)
                    .htmlContent(true)
                    .idModulo("PIAO")
                    .nomeModulo("PIAO")
                    .notificationMessage(mailObject)
                    .notificationSender(fromName)
                    .build();

                log.info("Invio email a <{}>, oggetto='{}'", toEmail, mailObject);
                return notificationService.sendEmail(emailTask)
                    .doOnNext(r -> log.info("Email inviata a {}", toEmail))
                    .onErrorResume(e -> {
                        log.error("Errore invio email a {}: {}", toEmail, e.getMessage());
                        return Mono.empty();
                    })
                    .then();
            })
            .onErrorResume(e -> {
                log.error("Errore nell'invio email a {}: {}", toEmail, e.getMessage(), e);
                return Mono.empty();
            });
    }

    /**
     * Invia un'email di approvazione PIAO con un link contenente le info del PIAO in Base64.
     * <p>
     * Flusso:
     * 1) Recupera il PiaoDTO dal BE tramite findById
     * 2) Costruisce {@link PiaoEmailInfoDTO} e lo serializza in JSON → Base64 URL-safe
     * 3) Legge dal DB la URL base (chiave {@code URL_APPROVAZIONE_PIAO})
     * 4) Costruisce URL completo: {@code urlBase?data=<base64>}
     * 5) Legge dal DB il body HTML (chiave {@code BODY_EMAIL_APPROVA_PIAO}) con placeholder {@code {{URL_APPROVAZIONE}}}
     * 6) Sostituisce il placeholder e invia l'email
     *
     * @param toEmail indirizzo email del destinatario
     * @param idPiao  id del PIAO da cui recuperare le informazioni
     * @return Mono<Void> che completa dopo l'invio dell'email
     */
    public Mono<Void> sendEmailApprovazione(String toEmail, Long idPiao) {
        log.info("Preparazione email approvazione PIAO a {} per idPiao={}", toEmail, idPiao);

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Indirizzo email destinatario mancante, email non inviata");
            return Mono.empty();
        }

        String mailObjectKey = "MAIL_OBJECT_" + AzioniEmail.APPROVA_PIAO.name();
        String mailBodyKey = "BODY_EMAIL_" + AzioniEmail.APPROVA_PIAO.name();
        String urlKey = "URL_APPROVAZIONE_PIAO";

        // 1) Recupera PiaoDTO + configurazioni in parallelo
        Mono<GenericResponseDTO<PiaoDTO>> piaoMono = piaoService.findById(idPiao);
        Mono<String> mailObjectMono = loadConfigValue(mailObjectKey, "Approvazione PIAO");
        Mono<String> mailBodyMono = loadConfigValue(mailBodyKey, "");
        Mono<String> urlBaseMono = loadConfigValue(urlKey, "");
        Mono<String> fromAddressMono = loadConfigValue(KeyNotification.FROM_ADDRESS.name(), "noreply@piao.gov.it");
        Mono<String> fromNameMono = loadConfigValue(KeyNotification.FROM_ADDRESS_NAME.name(), "PIAO");

        return Mono.zip(piaoMono, mailObjectMono, mailBodyMono, urlBaseMono, fromAddressMono, fromNameMono)
            .flatMap(tuple -> {
                GenericResponseDTO<PiaoDTO> piaoResp = tuple.getT1();
                String mailObject = tuple.getT2();
                String bodyTemplate = tuple.getT3();
                String urlBase = tuple.getT4();
                String fromAddress = tuple.getT5();
                String fromName = tuple.getT6();

                if (piaoResp.getData() == null) {
                    log.error("PIAO non trovato per id={}, email non inviata", idPiao);
                    return Mono.empty();
                }

                PiaoDTO piao = piaoResp.getData();

                // 2) Costruisci PiaoEmailInfoDTO e codifica in Base64
                PiaoEmailInfoDTO piaoInfo = PiaoEmailInfoDTO.builder()
                    .idPiao(piao.getId())
                    .denominazione(piao.getDenominazione())
                    .versione(piao.getVersione())
                    .tipologia(piao.getTipologia())
                    .tipologiaOnline(piao.getTipologiaOnline())
                    .denominazionePA(piao.getDenominazionePA())
                    .build();

                String base64Data;
                try {
                    String json = objectMapper.writeValueAsString(piaoInfo);
                    base64Data = Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(json.getBytes(StandardCharsets.UTF_8));
                    log.info("PiaoEmailInfoDTO codificato in Base64: {}", base64Data);
                } catch (Exception e) {
                    log.error("Errore serializzazione PiaoEmailInfoDTO: {}", e.getMessage(), e);
                    return Mono.empty();
                }

                // 3) Costruisci URL completo con query param data
                String separator = urlBase.contains("?") ? "&" : "?";
                String fullUrl = urlBase + separator + "data=" + base64Data;
                log.info("URL approvazione costruito: {}", fullUrl);

                // 4) Sostituisci il placeholder {{URL_APPROVAZIONE}} nel body HTML
                String mailBody = bodyTemplate.replace("{{URL_APPROVAZIONE}}", fullUrl);

                // 5) Costruisci e invia l'email
                MailAddressDTO from = MailAddressDTO.builder()
                    .email(fromAddress)
                    .name(fromName)
                    .build();

                MailAddressDTO toAddress = MailAddressDTO.builder()
                    .email(toEmail)
                    .name(toEmail)
                    .build();

                EmailTaskMessageDTO emailTask = EmailTaskMessageDTO.builder()
                    .fromAddress(from)
                    .toAddresses(List.of(toAddress))
                    .mailObject(mailObject)
                    .mailBody(mailBody)
                    .htmlContent(true)
                    .idModulo("PIAO")
                    .nomeModulo("PIAO")
                    .notificationMessage(mailObject)
                    .notificationSender(fromName)
                    .build();

                log.info("Invio email approvazione a <{}>, oggetto='{}'", toEmail, mailObject);
                return notificationService.sendEmail(emailTask)
                    .doOnNext(r -> log.info("Email approvazione inviata a {}", toEmail))
                    .onErrorResume(e -> {
                        log.error("Errore invio email approvazione a {}: {}", toEmail, e.getMessage());
                        return Mono.empty();
                    })
                    .then();
            })
            .onErrorResume(e -> {
                log.error("Errore nell'invio email approvazione a {}: {}", toEmail, e.getMessage(), e);
                return Mono.empty();
            });
    }

    /**
     * Carica il valore di una chiave dalla tabella configurazioni.
     * Se non trovata o in errore, ritorna il defaultValue.
     */
    private Mono<String> loadConfigValue(String key, String defaultValue) {
        return configurazioniService.getConfigurazioneByCodice(key)
            .map(response -> {
                if (response.getData() != null && response.getData().getValore() != null) {
                    return response.getData().getValore();
                }
                log.warn("Configurazione '{}' non trovata, uso default='{}'", key, defaultValue);
                return defaultValue;
            })
            .onErrorResume(e -> {
                log.warn("Errore recupero configurazione '{}': {}, uso default='{}'",
                    key, e.getMessage(), defaultValue);
                return Mono.just(defaultValue);
            })
            .defaultIfEmpty(defaultValue);
    }

    /**
     * Ricava il numero sezione dall'enum Sezione per il filtro utenti.
     * Es: SEZIONE_1 → "1", SEZIONE_21 → "21", SEZIONE_331 → "331"
     * Restituisce il numero grezzo senza punti, come atteso dal servizio utenti.
     */
    private String resolveNumeroSezione(Sezione sezione) {
        String name = sezione.name();
        if (!name.startsWith("SEZIONE_")) {
            return name.toLowerCase();
        }
        return name.substring("SEZIONE_".length());
    }

    /**
     * Ricava la label leggibile della sezione per i log.
     * Es: SEZIONE_1 → "1", SEZIONE_21 → "2.1", SEZIONE_331 → "3.3.1"
     */
    private String resolveNumeroSezioneLabel(Sezione sezione) {
        String name = sezione.name();
        if (!name.startsWith("SEZIONE_")) {
            return name.toLowerCase();
        }
        String numericPart = name.substring("SEZIONE_".length());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numericPart.length(); i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(numericPart.charAt(i));
        }
        return sb.toString();
    }
}
