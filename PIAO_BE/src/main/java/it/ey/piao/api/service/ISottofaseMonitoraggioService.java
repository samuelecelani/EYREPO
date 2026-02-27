package it.ey.piao.api.service;

import it.ey.dto.SottofaseMonitoraggioDTO;

import java.util.List;

public interface ISottofaseMonitoraggioService {
    SottofaseMonitoraggioDTO saveOrUpdate(SottofaseMonitoraggioDTO dto);

    List<SottofaseMonitoraggioDTO> getAllBySezione4 (Long idSezione4);

    void deleteById(Long id);


    /**
     * Carica i dati Mongo (attore) in un DTO già esistente
     */
    SottofaseMonitoraggioDTO loadMongoData(SottofaseMonitoraggioDTO dto);

    /**
     * Salva o aggiorna i dati Mongo (attore) associati al DTO
     */
    void saveMongoData(SottofaseMonitoraggioDTO response, SottofaseMonitoraggioDTO request);
}
