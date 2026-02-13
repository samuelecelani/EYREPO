package it.ey.piao.bff.service;

import it.ey.dto.DatiPubblicatiDTO;
import it.ey.dto.GenericResponseDTO;

import reactor.core.publisher.Mono;

import java.util.List;

public interface IDatiPubblicatiService {
    Mono<GenericResponseDTO<DatiPubblicatiDTO>> saveOrUpdate(DatiPubblicatiDTO request);

    Mono<GenericResponseDTO<List<DatiPubblicatiDTO>>> getAllByObbligoLegge(Long idObbligoLegge);

    Mono<Void> deleteById(Long id);
}
