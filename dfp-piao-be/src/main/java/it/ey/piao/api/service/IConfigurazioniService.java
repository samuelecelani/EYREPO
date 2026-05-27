package it.ey.piao.api.service;

import it.ey.dto.ConfigurazioniDTO;

import java.util.List;
import java.util.Map;

public interface IConfigurazioniService
{
    List<ConfigurazioniDTO> getAllConfigurazioni();
    ConfigurazioniDTO getConfigurazioneByCodice(String codice);
    void setValoreFromCodice(String codice, String valore);
    String getValoreFromCodice(String codice);

    /**
     * Recupera in un'unica chiamata i valori delle configurazioni
     * {@code DATA_COMPILAZIONE_PIAO} e {@code DATA_SCADENZA_PIAO} (richieste dal FE
     * al bootstrap, esposte tramite endpoint free).
     * <p>
     * La mappa ritornata ha come chiavi i codici della configurazione
     * e come valori la stringa salvata su DB (es. "dd/MM/yyyy"); il valore è {@code null}
     * se la configurazione non è presente.
     */
    Map<String, String> getPiaoDates();
}
