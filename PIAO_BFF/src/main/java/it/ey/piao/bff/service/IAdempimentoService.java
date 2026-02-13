package it.ey.piao.bff.service;

import it.ey.dto.AdempimentoDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IAdempimentoService
{
    public Mono<GenericResponseDTO<AdempimentoDTO>> saveOrUpdateAdempimento(AdempimentoDTO adempimentoDTO);
    public Mono<GenericResponseDTO<Void>> deleteAdempimento(Long id);
}
