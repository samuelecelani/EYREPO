package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.SottofaseMonitoraggioDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ISottofaseMonitoraggioService {
    Mono<GenericResponseDTO<SottofaseMonitoraggioDTO>> saveOrUpdate(SottofaseMonitoraggioDTO request);

    Mono<GenericResponseDTO<List<SottofaseMonitoraggioDTO>>> getAllBySezione4(Long idSezione4);

    Mono<Void> deleteById(Long id);
}
