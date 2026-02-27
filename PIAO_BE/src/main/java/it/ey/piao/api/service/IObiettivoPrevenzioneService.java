package it.ey.piao.api.service;

import it.ey.dto.ObiettivoPrevenzioneDTO;
import it.ey.entity.ObiettivoPrevenzione;

import java.util.List;

public interface IObiettivoPrevenzioneService {
    ObiettivoPrevenzioneDTO  saveOrUpdate(ObiettivoPrevenzioneDTO obiettivoPrevenzione);

    void  saveAll(List<ObiettivoPrevenzioneDTO> obiettiviPrevenzione);

    List<ObiettivoPrevenzioneDTO> getAllBySezione23(Long idSezione23);

    void deleteById(Long id);

    ObiettivoPrevenzioneDTO enrichWithRelations(ObiettivoPrevenzione entity); // Usato solo per il salvataggio

    void loadMongoDataForObiettivo(ObiettivoPrevenzioneDTO obiettivoDTO); // Carica solo i dati MongoDB
}
