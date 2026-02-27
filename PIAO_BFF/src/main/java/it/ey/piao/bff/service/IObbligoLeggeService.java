package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObbligoLeggeDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IObbligoLeggeService {
    Mono<GenericResponseDTO<ObbligoLeggeDTO>> saveOrUpdate(ObbligoLeggeDTO obbligoLegge);

    Mono<GenericResponseDTO<List<ObbligoLeggeDTO>>> getAllBySezione23(Long idSezione23);

    Mono<Void> deleteById(Long id);
}
