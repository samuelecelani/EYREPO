package it.ey.piao.bff.service;

import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAreaOrganizzativaService {
    Mono<GenericResponseDTO<List<AreaOrganizzativaDTO>>> findByidSezione1(Long idSezione1);
    Mono<GenericResponseDTO<List<AreaOrganizzativaDTO>>> findByPiaoId(Long piaoId);
    Mono<GenericResponseDTO<AreaOrganizzativaDTO>> save(AreaOrganizzativaDTO request);
}
