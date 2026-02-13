package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PrioritaPoliticaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IPrioritaPoliticaService {
    public Mono<GenericResponseDTO<List<PrioritaPoliticaDTO>>>findByidSezione1(Long idSezione1);
    public Mono<GenericResponseDTO<List<PrioritaPoliticaDTO>>> findByPiaoId(Long piaoId);
    public Mono<GenericResponseDTO<PrioritaPoliticaDTO>> save(PrioritaPoliticaDTO request);
}
