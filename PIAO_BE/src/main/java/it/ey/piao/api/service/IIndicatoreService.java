package it.ey.piao.api.service;

import it.ey.dto.IndicatoreDTO;
import it.ey.dto.StakeHolderDTO;
import it.ey.entity.Indicatore;

import java.util.List;
import java.util.Map;

public interface IIndicatoreService {
    public IndicatoreDTO save(IndicatoreDTO dto);
    public List<IndicatoreDTO> findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(Long idPiao, Long idEntitaFK, String codTipologiaFK);
    public IndicatoreDTO enrichWithRelations(Indicatore entity);

    /**
     * Carica in batch tutti gli indicatori con le loro relazioni MongoDB.
     * Ottimizzato per evitare N+1 query problem.
     * @param indicatoriIds lista di ID degli indicatori da caricare
     * @return mappa ID -> IndicatoreDTO con dati MongoDB caricati
     */
    Map<Long, IndicatoreDTO> findAllByIdsWithRelations(List<Long> indicatoriIds);
}
