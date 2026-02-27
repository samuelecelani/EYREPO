package it.ey.piao.api.service;

import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.entity.ObbiettivoPerformance;
import it.ey.enums.TipologiaObbiettivo;

import java.util.List;

public interface IObbiettivoPerformanceService {

    ObbiettivoPerformanceDTO saveOrUpdate(ObbiettivoPerformanceDTO obbiettivoPerformance);

    List<ObbiettivoPerformanceDTO> saveOrUpdateAll(List<ObbiettivoPerformanceDTO> obiettiviPerformance);

    List<ObbiettivoPerformanceDTO> getAllBySezione22(Long idSezione22);

    /**
     * Recupera gli obiettivi di performance filtrati per tipologia (obbligatoria),
     * idOvp e idStrategia (opzionali).
     * Se idOvp o idStrategia sono null, verranno cercati record con il campo corrispondente null sul DB.
     */
    List<ObbiettivoPerformanceDTO> findByTipologiaAndFilters(TipologiaObbiettivo tipologia, Long idOvp, Long idStrategia);

    void deleteById(Long id);

    ObbiettivoPerformanceDTO enrichWithRelations(ObbiettivoPerformance entity); // Usato solo per il salvataggio

    void loadMongoDataForObiettivo(ObbiettivoPerformanceDTO obiettivoDTO); // Carica solo i dati MongoDB
}
