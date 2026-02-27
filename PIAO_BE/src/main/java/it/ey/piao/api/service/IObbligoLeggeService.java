package it.ey.piao.api.service;

import it.ey.dto.ObbligoLeggeDTO;
import it.ey.entity.ObbligoLegge;

import java.util.List;

public interface IObbligoLeggeService {
    ObbligoLeggeDTO saveOrUpdate(ObbligoLeggeDTO obbligoLegge);

    List<ObbligoLeggeDTO> getAllBySezione23(Long idSezione23);

    void deleteById(Long id);

    ObbligoLeggeDTO enrichWithRelations(ObbligoLegge entity); // Usato solo per il salvataggio

    void loadMongoDataForObbligo(ObbligoLeggeDTO obbligoDTO); // Carica solo i dati MongoDB
}
