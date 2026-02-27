package it.ey.piao.api.service;

import it.ey.dto.CategoriaObiettiviDTO;

import java.util.List;

public interface ICategoriaObiettiviService {

    CategoriaObiettiviDTO saveOrUpdate(CategoriaObiettiviDTO dto);

    List<CategoriaObiettiviDTO> getAllBySezione4(Long idSezione4);

    void deleteById(Long id);

    /**
     * Carica i dati Mongo (attore, attivita) in un DTO già esistente
     */
    CategoriaObiettiviDTO loadMongoData(CategoriaObiettiviDTO dto);

    /**
     * Salva o aggiorna i dati Mongo (attore, attivita) associati al DTO
     */
    void saveMongoData(CategoriaObiettiviDTO response, CategoriaObiettiviDTO request);
}
