package it.ey.piao.api.service;

import it.ey.dto.AdempimentoDTO;

public interface IAdempimentoService
{
    AdempimentoDTO saveOrUpdate(AdempimentoDTO adempimentoDTO);
    void deleteAdempimento(Long id);
}
