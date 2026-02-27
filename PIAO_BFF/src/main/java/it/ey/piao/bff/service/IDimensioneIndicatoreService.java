package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.DimensioneIndicatoreDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IDimensioneIndicatoreService {
    Mono<GenericResponseDTO<List<DimensioneIndicatoreDTO>>> getDimensioneIndicatore(String codTipologiaFK);
}

