package it.ey.piao.api.service;

import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;

import java.util.List;

public interface IStrutturaPiaoService {

    List<StrutturaPiaoDTO> getAllStruttura(Long idPiao);

    /**
     * Ritorna la lista flat (solo foglie) con i campi di validazione:
     * triennio, stato rimappato, utente/data invio richiesta, utente/data validazione.
     */
    List<StrutturaValidazioneDTO> getAllStrutturaFromValidazione(Long idPiao);
}
