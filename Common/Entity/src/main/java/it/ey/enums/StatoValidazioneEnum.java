package it.ey.enums;

public enum StatoValidazioneEnum {

    RICHIESTA_DA_INVIARE(1L, "Richiesta di validazione da inviare"),
    DA_VALIDARE(2L, "Da validare"),
    VALIDATA(3L, "Validata"),
    RIFIUTATA(4L, "Rifiutata"),
    VALIDAZIONE_REVOCATA(5L, "Validazione Revocata");

    private final Long id;
    private final String descrizione;

    StatoValidazioneEnum(Long id, String descrizione) {
        this.id = id;
        this.descrizione = descrizione;
    }

    public Long getId() { return id; }
    public String getDescrizione() { return descrizione; }

    public static StatoValidazioneEnum fromDescrizione(String descrizione) {
        if (descrizione == null) {
            throw new IllegalArgumentException("Descrizione stato validazione nulla");
        }
        for (StatoValidazioneEnum s : values()) {
            if (s.descrizione.equalsIgnoreCase(descrizione.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Descrizione stato validazione non valida: " + descrizione);
    }
}
