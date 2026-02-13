package it.ey.piao.api.service;

import it.ey.dto.AreaOrganizzativaDTO;

import java.util.List;

public interface IAreaOrganizzativaService {
    List<AreaOrganizzativaDTO> findByidSezione1(Long idSezione1);
    List<AreaOrganizzativaDTO> findByPiaoId(Long piaoId);
    AreaOrganizzativaDTO save(AreaOrganizzativaDTO areaOrganizzativa);
}
