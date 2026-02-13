package it.ey.piao.bff.service;

import it.ey.dto.FaseDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IFaseService {
    public Mono<GenericResponseDTO<FaseDTO>> saveOrUpdateFase(FaseDTO fase);

    public Mono<GenericResponseDTO<Void>> deleteFase(Long id);
}
