package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.enums.TipologiaObbiettivo;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IObbiettivoPerformanceService {
    Mono<GenericResponseDTO<ObbiettivoPerformanceDTO>> saveOrUpdate(ObbiettivoPerformanceDTO request);

    Mono<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>> getAllBySezione22(Long idSezione22);

    Mono<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>> findByTipologiaAndFilters(TipologiaObbiettivo tipologia, Long idOvp, Long idStrategia);

    Mono<Void> deleteById(Long id);
}
