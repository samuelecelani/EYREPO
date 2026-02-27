package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StakeHolderDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStakeholderService {
    public Mono<GenericResponseDTO<List<StakeHolderDTO>>> findByidPiao(Long idPiao);
    public Mono<GenericResponseDTO<StakeHolderDTO>> save(StakeHolderDTO request);
    Mono<Void> deleteById(Long id);


}
