package it.ey.piao.api.service;

import it.ey.dto.AdempimentiNormativiDTO;

public interface IAdempimentiNormativiService
{
    AdempimentiNormativiDTO saveOrUpdate(AdempimentiNormativiDTO adempimentoNormativoDTO);
    void deleteAdempimentoNormativo(Long id);
}
