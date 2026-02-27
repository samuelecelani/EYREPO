package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.TargetIndicatoreDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ITargetIndicatoreService {
    Mono<GenericResponseDTO<List<TargetIndicatoreDTO>>> getTargetIndicatore();
}

