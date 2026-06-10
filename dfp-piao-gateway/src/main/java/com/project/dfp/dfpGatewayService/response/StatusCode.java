package com.project.dfp.dfpGatewayService.response;

import lombok.Getter;

@Getter
public enum StatusCode {

    /** common success **/
    GEN_200("Operazione avvenuta con successo"),

    /** Authentication errors **/
    AUTH_500("Autenticazione fallita"),
    AUTH_501("Account bloccato"),
    AUTH_502("Credenziali scadute"),
    AUTH_503("Account scaduto"),
    AUTH_504("Account disabilitato"),
    AUTH_403("Accesso negato"),

    /** Validation errors **/
    VAL_400("Dati di input non validi"),

    /** Generic errors **/
    GEN_401("Violazione vincolo integrità dati"),
    GEN_501("Errore interno del server"),
    GEN_404("Risorsa non trovata"),

    /** user errors **/
    GEN_405("Utente non trovato"),
    GEN_406("Amministrazione non trovata"),
    GEN_407("Ruolo non trovato"),
    GEN_408("Utente non presente nell'amministrazione"),
    GEN_409("Amministrazione non trovata"),
    GEN_410("Tipologia ruolo non trovata");


    private final String description;

    StatusCode(String description) {
        this.description = description;
    }
}
