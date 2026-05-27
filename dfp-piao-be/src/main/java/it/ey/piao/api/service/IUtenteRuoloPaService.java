package it.ey.piao.api.service;


import it.ey.dto.UtenteRuoliPaSezioneDTO;

import java.util.List;

public interface IUtenteRuoloPaService {

    /**
     * Salva le sezioni associate a un utente esterno per una specifica amministrazione.
     * Richiede sempre la tripla: externalUserId + idAmministrazione + sezioni.
     */
    List<UtenteRuoliPaSezioneDTO> saveSezioni(String externalUserId, String idAmministrazione, List<UtenteRuoliPaSezioneDTO> sezioni);

    /**
     * Recupera le sezioni associate a un utente esterno per una specifica amministrazione.
     */
    List<UtenteRuoliPaSezioneDTO> findSezioniByExternalUserIdAndIdAmministrazione(String externalUserId, String idAmministrazione);

    /**
     * Recupera tutti gli utenti/sezioni per una data amministrazione.
     */
    List<UtenteRuoliPaSezioneDTO> findSezioniByIdAmministrazione(String idAmministrazione);

    /**
     * Elimina tutte le sezioni associate a un utente esterno per una specifica amministrazione.
     */
    void deleteSezioniByExternalUserIdAndIdAmministrazione(String externalUserId, String idAmministrazione);
}
