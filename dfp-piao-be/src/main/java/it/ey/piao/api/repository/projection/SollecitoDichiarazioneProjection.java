package it.ey.piao.api.repository.projection;

/**
 * Proiezione (interface-based) usata dalla query paginata di ricerca dei solleciti.
 * Evita l'idratazione delle entità Piao/DichiarazioneScadenza/Sezione1/Anagrafica:
 * Hibernate seleziona solo le colonne dichiarate qui sotto.
 */
public interface SollecitoDichiarazioneProjection
{
    Long getIdPiao();
    String getCodePA();
    String getAmministrazione();
    /** true se esiste DichiarazioneScadenza collegata al PIAO, false altrimenti. */
    Boolean getInviata();
}

