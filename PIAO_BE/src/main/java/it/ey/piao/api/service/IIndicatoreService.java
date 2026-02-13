package it.ey.piao.api.service;

import it.ey.dto.IndicatoreDTO;
import it.ey.dto.StakeHolderDTO;
import it.ey.entity.Indicatore;

import java.util.List;

public interface IIndicatoreService {
    public IndicatoreDTO save(IndicatoreDTO dto);
    public List<IndicatoreDTO> findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(Long idPiao, Long idEntitaFK, String codTipologiaFK);
    public IndicatoreDTO enrichWithRelations(Indicatore entity);
}
