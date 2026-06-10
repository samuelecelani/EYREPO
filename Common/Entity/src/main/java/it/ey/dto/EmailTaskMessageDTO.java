package it.ey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO che rappresenta un messaggio email da inviare sulla coda ActiveMQ
 * dove è in ascolto il modulo dfp-email.
 * Rispecchia la struttura di EmailTaskMessage nel modulo dfp-email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailTaskMessageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * UUID di riferimento univoco per tracciare il messaggio.
     */
    private String referenceUuid;

    /**
     * Mittente dell'email (opzionale, se null usa il default di dfp-email).
     */
    private MailAddressDTO fromAddress;

    /**
     * Destinatari principali (TO).
     */
    private List<MailAddressDTO> toAddresses;

    /**
     * Destinatari in copia (CC).
     */
    private List<MailAddressDTO> ccAddresses;

    /**
     * Destinatari in copia nascosta (BCC).
     */
    private List<MailAddressDTO> bccAddresses;

    /**
     * Oggetto dell'email.
     */
    private String mailObject;

    /**
     * Corpo dell'email.
     */
    private String mailBody;

    /**
     * Se true, il corpo è HTML; se false, è plain text.
     */
    private boolean htmlContent;

    /**
     * Identificativo del modulo chiamante (es. "PERFORMANCE").
     */
    private String idModulo;

    /**
     * Nome del modulo chiamante (es. "Performance").
     */
    private String nomeModulo;

    // ---- Campi per la notifica di callback (dfp-email → dfp-notifiche) ----

    /**
     * Messaggio della notifica che verrà creata dopo l'invio dell'email.
     */
    private String notificationMessage;

    /**
     * Mittente della notifica (es. "Sistema").
     */
    private String notificationSender;

    /**
     * Codice fiscale del destinatario della notifica.
     */
    private String notificationCodiceFiscale;

    /**
     * Codice PA del destinatario della notifica.
     */
    private String notificationCodicePa;

    /**
     * ID amministrazione del destinatario della notifica.
     */
    private String notificationAmministrazioneId;
}

