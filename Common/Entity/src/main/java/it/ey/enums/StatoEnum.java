package it.ey.enums;
public enum StatoEnum {
    DA_COMPILARE(1L,"Da compilare"),
    IN_COMPILAZIONE(2L, "In compilazione"),
    COMPILATA(3L, "Compilata"),
    IN_VALIDAZIONE(4L, "In validazione"),
    VALIDATA(5L, "Validata"),
    APPROVATO(6L, "Approvata"),
    PUBBLICATO(7L, "Pubblicata");

    private final Long id;
    private final String descrizione;

    StatoEnum(Long id, String descrizione) {
        this.id = id;
        this.descrizione = descrizione;
    }

    public Long getId() { return id; }
    public String getDescrizione() { return descrizione; }

    public static StatoEnum fromDescrizione(String descrizione) {
        if (descrizione == null) {
            throw new IllegalArgumentException("Descrizione stato nulla");
        }
        for (StatoEnum s : values()) {
            if (s.descrizione.equalsIgnoreCase(descrizione.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Descrizione stato non valida: " + descrizione);
    }
}
