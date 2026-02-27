package it.ey.piao.bff.service;



import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NovitaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface INovitaService {
    Mono<GenericResponseDTO<List<NovitaDTO>>> getNovita(String modulo);
}

