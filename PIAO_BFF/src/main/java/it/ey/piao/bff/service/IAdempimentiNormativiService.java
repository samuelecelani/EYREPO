package it.ey.piao.bff.service;

import it.ey.dto.AdempimentiNormativiDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IAdempimentiNormativiService
{
    public Mono<GenericResponseDTO<AdempimentiNormativiDTO>> saveOrUpdateAdempimento(AdempimentiNormativiDTO adempimentoNormativoDTO);
    public Mono<GenericResponseDTO<Void>> deleteAdempimentoNormativo(Long id);
}
