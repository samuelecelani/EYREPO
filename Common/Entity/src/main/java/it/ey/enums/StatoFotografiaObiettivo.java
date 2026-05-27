package it.ey.enums;

public enum StatoFotografiaObiettivo
{
    FOTOGRAFIA_FORMAZIONE(1L, "Fotografia formazione"),
    OBIETTIVI_RISULTATI(2L, "Obiettivi risultati");

    private final Long id;
    private final String descrizione;

    StatoFotografiaObiettivo(Long id, String descrizione)
    {
        this.id = id;
        this.descrizione = descrizione;
    }

    public Long getId()
    {
        return id;
    }

    public String getDescrizione()
    {
        return descrizione;
    }
}