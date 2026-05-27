package it.ey.enums;
public enum StatoEnum {
    DA_COMPILARE(1L,"Da compilare"),
    IN_COMPILAZIONE(2L, "In compilazione"),
    COMPILATA(3L, "Compilata"),
    IN_VALIDAZIONE(4L, "In validazione"),
    VALIDATA(5L, "Validata"),
    RICHIESTA_APPROVAZIONE(6L, "Richiesta Approvazione"),
    APPROVATO(7L, "Approvata"),
    PUBBLICATO(8L, "Pubblicata");

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
    public static StatoEnum fromId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id stato nulla");
        }
        for (StatoEnum s : values()) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Id stato non valida: " + id);
    }
}
