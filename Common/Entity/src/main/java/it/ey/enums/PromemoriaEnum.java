package it.ey.enums;

public enum PromemoriaEnum {
    GIORNO_PRIMA(1L,"Un giorno prima"),
    GIORNO_STESSO_ORE_9(2L, "In quel giorno alle 9:00"),
    GIORNO_PRIMA_ORE_9(3L, "Il giorno prima alle 9:00"),
    DUE_GIORNI_PRIMA_ORE_9(4L, "2 giorni prima alle 9:00"),
    SETTIMANA_PRIMA_ORE_9(5L, "1 settimana prima alle 9:00"),
    PERSONALIZZATO(6L, "Personalizzato");

    private final Long id;
    private final String descrizione;

    PromemoriaEnum(Long id, String descrizione)
    {
        this.id = id;
        this.descrizione = descrizione;
    }

    public static PromemoriaEnum fromId(Long id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id tipo promemoria null");
        }
        for (PromemoriaEnum p : values())
        {
            if (p.id.equals(id))
            {
                return p;
            }
        }
        throw new IllegalArgumentException("Idpromemoria non valido: " + id);
    }
}
