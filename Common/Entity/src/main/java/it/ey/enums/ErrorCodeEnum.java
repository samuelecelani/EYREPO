package it.ey.enums;

public enum ErrorCodeEnum {

    /**
     * Enum centralizzato per la gestione dei codici errore funzionali relativi
     * ai vincoli di cancellazione e modifica tra le entità del PIAO.


     -
     * Ogni voce dell'enum contiene:
     * - un codice numerico LONG
     * - un messaggio descrittivo user-friendly pronto per essere ritornato al frontend.


     * - Il nome della costante segue lo schema ENTITÀ_USO_SEZIONE.
     * - Il codice numerico aumenta di 10 per categorie funzionali omogenee.

     */
    OVP_USATO_IN_SEZIONE22(
            900L,
            "Impossibile eliminare o modificare OVP: utilizzato nella Sezione 2.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    OVP_USATO_IN_SEZIONE23(
            901L,
            "Impossibile eliminare o modificare OVP: utilizzato nella Sezione 2.3 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    OVP_USATO_IN_SEZIONE31(
            902L,
            "Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    OVP_USATO_IN_SEZIONE32(
            903L,
            "Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    OVP_USATO_IN_SEZIONE331(
            904L,
            "Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    OVP_USATO_IN_SEZIONE332(
            905L,
            "Impossibile eliminare o modificare OVP: utilizzato nella Sezione 3.3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    OBIETTIVO_PERFORMANCE_USATO_SEZIONE23(
            910L,
            "Impossibile eliminare o modificare Obiettivo Performance: collegato alla Sezione 2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STRATEGIA_USATA_SEZIONE22(
            920L,
            "Impossibile eliminare o modificare la Strategia: collegata a Obiettivi Performance in Sezione 2.2 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STRATEGIA_USATA_SEZIONE23(
            921L,
            "Impossibile eliminare o modificare la Strategia: collegata a Obiettivi Corruzione/Trasparenza in Sezione 2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_OVP(
            930L,
            "Impossibile eliminare lo Stakeholder: collegato a OVP in Sezione2.1 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_OBIETTIVO_PERFORMANCE(
            931L,
            "Impossibile eliminare lo Stakeholder: collegato a Obiettivo Performance in Sezione2.2 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_MISURA_PREVENZIONE(
            932L,
            "Impossibile eliminare lo Stakeholder: collegato a Misura di prevenzione in Sezione2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_MISURA_PREVENZIONE_EVENTO_RISCHIOSO(
            933L,
            "Impossibile eliminare lo Stakeholder: collegato a Misura di prevenzione Evento Rischioso in Sezione2.3 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),
    STAKEHOLDER_USATO_SEZIONE31(
            934L,
            "Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_SEZIONE32(
            935L,
            "Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_SEZIONE331(
            936L,
            "Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.3.1 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),

    STAKEHOLDER_USATO_SEZIONE332(
            937L,
            "Impossibile eliminare lo Stakeholder: collegato alla Sezione 3.3.2 in stato IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),
    AREA_ORGANIZZATIVA_USATA_OVP(
            940L,
            "Impossibile eliminare l 'Area Organizzativa: collegata a OVP in Sezione2.1 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    ),
    PRIORITA_POLITICA_USATA_OVP(
            950L,
            "Impossibile eliminare la Priorita Politica: collegata a OVP in Sezione2.1 IN VALIDAZIONE/VALIDATA/APPROVATO/PUBBLICATO"
    );

    private final Long id;
    private final String description;

    ErrorCodeEnum(Long id, String description) {
        this.id = id;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}