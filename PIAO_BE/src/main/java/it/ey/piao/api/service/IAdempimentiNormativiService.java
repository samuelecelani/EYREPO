package it.ey.piao.api.service;

import it.ey.dto.AdempimentiNormativiDTO;

public interface IAdempimentiNormativiService
{
    void saveOrUpdate(AdempimentiNormativiDTO adempimentoNormativoDTO);
    void saveOrUpdateAll(java.util.List<AdempimentiNormativiDTO> adempimentiNormativi);
    void deleteAdempimentoNormativo(Long id);
}
