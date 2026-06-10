package it.ey.piao.api.service;

import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;

import java.util.List;
import java.util.Map;

public interface IStrutturaPiaoService {

    List<StrutturaPiaoDTO> getAllStruttura(Long idPiao, String userNameSurname, String userRole);

    /**
     * Ritorna la lista flat (solo foglie) con i campi di validazione:
     * triennio, stato rimappato, utente/data invio richiesta, utente/data validazione.
     */
    List<StrutturaValidazioneDTO> getAllStrutturaFromValidazione(Long idPiao);

    void accettaValidazioneSezioniSelezionate(Long idPiao, Map<String,Long> sezioneIdToTipologia);

    /**
     * Ritorna la lista flat delle sezioni/sottosezioni "effettive" (foglie) del PIAO,
     * escluso il nodo root "0". Es: 1, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3.1, 3.3.2, 4
     */
    List<StrutturaPiaoDTO> getAllStrutturaEffective();
}
