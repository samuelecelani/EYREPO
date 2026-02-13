package it.ey.piao.api.service;

import it.ey.dto.AttoreDTO;

import java.util.List;

public interface IAttoreService {
    List<AttoreDTO> findListByIdPiao(Long idPiao);
    AttoreDTO save(Long idPiao, AttoreDTO attore);
}
