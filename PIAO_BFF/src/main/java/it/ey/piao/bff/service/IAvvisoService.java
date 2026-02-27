package it.ey.piao.bff.service;


import it.ey.dto.AvvisoDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAvvisoService {
    Mono<GenericResponseDTO<List<AvvisoDTO>>> getAvvisi(String modulo);
}

