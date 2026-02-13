package it.ey.piao.bff.service;

import it.ey.dto.AttivitaSensibileDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAttivitaSensibileService {
    Mono<GenericResponseDTO<AttivitaSensibileDTO>> saveOrUpdate(AttivitaSensibileDTO attivitaSensibileDTO);

    Mono<GenericResponseDTO<List<AttivitaSensibileDTO>>> getAllBySezione23(Long idSezione23);

    Mono<Void> deleteById(Long id);
}
