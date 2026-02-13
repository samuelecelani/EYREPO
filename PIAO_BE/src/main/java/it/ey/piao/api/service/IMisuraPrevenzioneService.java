package it.ey.piao.api.service;

import it.ey.dto.MisuraPrevenzioneDTO;
import it.ey.entity.MisuraPrevenzione;

import java.util.List;

public interface IMisuraPrevenzioneService {

    MisuraPrevenzioneDTO saveOrUpdate(MisuraPrevenzioneDTO misuraPrevenzione);

    List<MisuraPrevenzioneDTO> getAllByObiettivoPrevenzione(Long idObiettivoPrevenzione);
    List<MisuraPrevenzioneDTO> getAllBySezione23(Long idSezione23);

    void deleteById(Long id);

    MisuraPrevenzioneDTO enrichWithRelations(MisuraPrevenzione entity); // Usato solo per il salvataggio

    void loadMongoDataForMisura(MisuraPrevenzioneDTO misuraDTO); // Carica solo i dati MongoDB
}
