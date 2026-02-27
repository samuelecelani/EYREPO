package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IObiettivoPrevenzioneCorruzioneTrasparenzaService{

Mono<GenericResponseDTO<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>> saveOrUpdate(ObiettivoPrevenzioneCorruzioneTrasparenzaDTO request);

Mono<GenericResponseDTO<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>>> getAllBySezione23(Long idSezione23);

Mono<Void> deleteById(Long id);
}
