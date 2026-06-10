package it.ey.enums;

public enum TipoTabellaSezione331 {
    //PAC & PAL
    CESSAZIONI_ANNO_CORRENTE("Prospetto previsionale cessazioni dal servizio e risparmio su base annua per l'anno corrente"),
    VALORE_FINANZIARIO_DOTAZIONE_ORGANICA("Valore finanziario della dotazione organica al 31.12 dell'anno precedente"),
    CONSISTENZA_PERSONALE_DIRIGENZIALE("Rappresentazione della consistenza di personale dirigenziale al 31.12 dell'anno precedente"),
    CONSISTENZA_PERSONALE_NON_DIRIGENZIALE("Rappresentazione della consistenza di personale non dirigenziale al 31.12 dell'anno precedente"),
    RIEPILOGO_ASSUNZIONI_DIRIGENZIALE("Riepilogo assunzioni al 31.12 dell'anno precedente - Tabella del personale dirigenziale"),
    RIEPILOGO_ASSUNZIONI_AREE_CONTRATTUALI("Riepilogo assunzioni al 31.12 dell'anno precedente - Tabella delle aree contrattuali"),
    RIEPILOGO_CESSAZIONI("Riepilogo cessazioni al 31.12 dell'anno precedente"),
    DOTAZIONE_ORGANICA_RIMODULAZIONE("Dotazione organica in seguito alla rimodulazione"),
    COPERTURA_FABBISOGNO_ANNO_CORRENTE_DIRIGENZIALE("Copertura del fabbisogno dell'anno corrente - Tabella del personale dirigenziale"),
    COPERTURA_FABBISOGNO_ANNO_CORRENTE_AREE_CONTRATTUALI("Copertura del fabbisogno dell'anno corrente - Tabella delle aree contrattuali"),
    COPERTURA_FABBISOGNO_ANNO1_DIRIGENZIALE("Prospetto previsionale della copertura del fabbisogno per il primo anno successivo all'anno corrente - Tabella del personale dirigenziale"),
    COPERTURA_FABBISOGNO_ANNO1_AREE_CONTRATTUALI("Prospetto previsionale della copertura del fabbisogno per il primo anno successivo all'anno corrente - Tabella delle aree contrattuali"),
    CESSAZIONI_SERVIZIO("Prospetto previsionale cessazioni dal servizio e risparmio su base annua per il primo anno successivo all'anno corrente"),
    COPERTURA_FABBISOGNO_ANNO2_DIRIGENZIALE("Prospetto previsionale della copertura del fabbisogno per il secondo anno successivo all'anno corrente - Tabella del personale dirigenziale"),
    COPERTURA_FABBISOGNO_ANNO2_AREE_CONTRATTUALI("Prospetto previsionale della copertura del fabbisogno per il secondo anno successivo all'anno corrente - Tabella delle aree contrattuali"),
    //Nuove solo per PAL
    CONSISTENZA_PERSONALE_DIRIGENZIALE_TEMPO_DETERMINATO("Rappresentazione della consistenza di personale dirigenziale a tempo determinato al 31.12 dell’anno precedente"),
    CONSISTENZA_PERSONALE_NON_DIRIGENZIALE_TEMPO_DETERMINATO("Rappresentazione della consistenza di personale non dirigenziale a tempo determinato al 31.12 dell’anno precedente"),
    RENDICONTI_ENTRATE_ULTIMI_3_ANNI("Prospetto calcolo facolta assunzioni - Tabella delle entrate degli ultimi tre anni rendiconti approvato"),
    SPESE_PERSONALE_ANNO_T2("Prospetto calcolo facolta assunzioni - Tabella delle spese del personale anno t-2"),
    VERIFICA_VALORE_SOGLIA("Prospetto calcolo facolta assunzioni - Verifica del valore soglia"),
    LIMITE_SPESA_PERSONALE_2011_2013("Limiti spesa del personale - Tabella limite spesa del personale 2011-2013"),
    LIMITE_SPESA_PERSONALE("Limiti spesa del personale - Tabella spesa personale");


    private final String descrizione;

    TipoTabellaSezione331(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
