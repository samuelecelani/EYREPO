package it.ey.piao.api.service;

import it.ey.dto.AttoreDTO;
import it.ey.enums.Sezione;

import java.util.List;

public interface IAttoreService {
    List<AttoreDTO> findListByIdPiao(Long idPiao);
    AttoreDTO save(Long idPiao, AttoreDTO attore);
    AttoreDTO findByExternalIdAndTipoSezione(Long externalId, Sezione tipoSezione, Long externalIdFK);
    List<AttoreDTO> findByIdPiaoAndTipoSezione(Long idPiao, Sezione tipoSezione);
    AttoreDTO findByExternalIdFKAndTipoSezione(Long externalIdFK, Sezione tipoSezione);
}
