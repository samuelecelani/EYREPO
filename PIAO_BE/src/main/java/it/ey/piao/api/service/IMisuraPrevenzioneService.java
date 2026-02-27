package it.ey.piao.api.service;

import it.ey.dto.MisuraPrevenzioneDTO;
import it.ey.entity.MisuraPrevenzione;

import java.util.List;

public interface IMisuraPrevenzioneService {

    void saveOrUpdate(MisuraPrevenzioneDTO misuraPrevenzione);

    void  saveAll(List<MisuraPrevenzioneDTO> misurePrevenzione);

    List<MisuraPrevenzioneDTO> getAllByObiettivoPrevenzione(Long idObiettivoPrevenzione);
    List<MisuraPrevenzioneDTO> getAllBySezione23(Long idSezione23);

    void deleteById(Long id);

    /**
     * Imposta a NULL il riferimento obiettivoPrevenzione per tutte le MisuraPrevenzione collegate.
     * Usato prima di eliminare un ObiettivoPrevenzione per non cancellare le misure.
     */
    int setObiettivoPrevenzioneToNullByObiettivoId(Long idObiettivo);

    MisuraPrevenzioneDTO enrichWithRelations(MisuraPrevenzione entity); // Usato solo per il salvataggio

    void loadMongoDataForMisura(MisuraPrevenzioneDTO misuraDTO); // Carica solo i dati MongoDB
}
