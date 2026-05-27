package it.ey.enums;

/**
 * Stato della Dichiarazione di mancata o ritardata compilazione del PIAO.
 * <ul>
 *     <li>{@link #INVIATA}: esiste una DichiarazioneScadenza collegata al PIAO</li>
 *     <li>{@link #NON_INVIATA}: NON esiste alcuna DichiarazioneScadenza collegata al PIAO</li>
 * </ul>
 */
public enum StatoDichiarazioneEnum
{
    INVIATA,
    NON_INVIATA;
}

