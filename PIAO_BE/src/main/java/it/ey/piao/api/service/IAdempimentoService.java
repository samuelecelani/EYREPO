package it.ey.piao.api.service;

import it.ey.dto.AdempimentoDTO;

public interface IAdempimentoService
{
    void saveOrUpdate(AdempimentoDTO adempimentoDTO);
    void saveOrUpdateAll(java.util.List<AdempimentoDTO> adempimenti);
    AdempimentoDTO loadMongoDataAdempimento(AdempimentoDTO adempimento);
    void saveMongoDataAdempimento(AdempimentoDTO response, AdempimentoDTO request);
    void deleteAdempimento(Long id);
}
