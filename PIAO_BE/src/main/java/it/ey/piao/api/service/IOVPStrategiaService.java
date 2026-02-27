package it.ey.piao.api.service;

import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import it.ey.entity.OVPStrategia;

import java.util.List;

public interface IOVPStrategiaService {

    OVPStrategiaDTO save(OVPStrategiaDTO request, Long idOVP);

    /**
     * Salva o aggiorna una lista di OVPStrategia in batch.
     * @param requests lista di OVPStrategiaDTO da salvare
     * @param idOVP ID dell'OVP padre
     */
    void saveAll(List<OVPStrategiaDTO> requests, Long idOVP);

    /**
     * Recupera tutte le strategie associate a un OVP.
     * @param idOvp ID dell'OVP padre
     * @return lista di OVPStrategiaDTO con dati MongoDB caricati
     */
    List<OVPStrategiaDTO> findByOvpId(Long idOvp);

    void delete(Long id);

    OVPStrategiaDTO enrichWithRelations(OVPStrategia entity); // Usato solo per il salvataggio

    /**
     * Sincronizza la lista di Indicatori della strategia.
     * Gli indicatori vengono impostati con il back-reference verso la strategia.
     */
    void syncIndicatori(OVPStrategia parent, List<OVPStrategiaIndicatoreDTO> dtoList);

    void loadMongoDataForStrategia(OVPStrategiaDTO strategiaDTO); // Carica solo i dati MongoDB (indicatori)
}
