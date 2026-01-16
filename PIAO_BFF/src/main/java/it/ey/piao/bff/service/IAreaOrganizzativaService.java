package it.ey.piao.bff.service;

import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAreaOrganizzativaService {
    public Mono<GenericResponseDTO<List<AreaOrganizzativaDTO>>> findByidSezione1(Long idSezione1);
    public Mono<GenericResponseDTO<AreaOrganizzativaDTO>> save(AreaOrganizzativaDTO request);
}
