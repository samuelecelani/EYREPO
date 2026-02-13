package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObbiettivoPerformanceDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IObbiettivoPerformanceService {
    Mono<GenericResponseDTO<ObbiettivoPerformanceDTO>> saveOrUpdate(ObbiettivoPerformanceDTO request);

    Mono<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>> getAllBySezione22(Long idSezione22);

    Mono<Void> deleteById(Long id);
}
