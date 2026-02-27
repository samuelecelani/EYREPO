package it.ey.piao.api.service;

import it.ey.dto.AttoreDTO;
import it.ey.enums.Sezione;

import java.util.List;

public interface IAttoreService {
    List<AttoreDTO> findListByIdPiao(Long idPiao);
    AttoreDTO save(Long idPiao, AttoreDTO attore);
    AttoreDTO findByExternalIdAndTipoSezione(Long externalId, Sezione tipoSezione);
}
