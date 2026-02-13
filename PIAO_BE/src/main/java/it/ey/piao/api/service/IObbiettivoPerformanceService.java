package it.ey.piao.api.service;

import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.entity.ObbiettivoPerformance;

import java.util.List;

public interface IObbiettivoPerformanceService {

    ObbiettivoPerformanceDTO saveOrUpdate(ObbiettivoPerformanceDTO obbiettivoPerformance);

    List<ObbiettivoPerformanceDTO> saveOrUpdateAll(List<ObbiettivoPerformanceDTO> obiettiviPerformance);

    List<ObbiettivoPerformanceDTO> getAllBySezione22(Long idSezione22);

    void deleteById(Long id);

    ObbiettivoPerformanceDTO enrichWithRelations(ObbiettivoPerformance entity); // Usato solo per il salvataggio

    void loadMongoDataForObiettivo(ObbiettivoPerformanceDTO obiettivoDTO); // Carica solo i dati MongoDB
}
