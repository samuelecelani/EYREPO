package it.ey.piao.bff.service;


import it.ey.dto.AvvisoDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAvvisoService {
    Mono<GenericResponseDTO<List<AvvisoDTO>>> getAll();
    Mono<GenericResponseDTO<AvvisoDTO>> getById(Long id);
    Mono<GenericResponseDTO<AvvisoDTO>> create(AvvisoDTO avvisoDTO);
    Mono<GenericResponseDTO<AvvisoDTO>> update(Long id, AvvisoDTO avvisoDTO);
    Mono<GenericResponseDTO<Void>> delete(Long id);
}

