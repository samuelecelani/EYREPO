package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ProceduraDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IProceduraService {
    public Mono<GenericResponseDTO<List<ProceduraDTO>>> getProcedure (Long idSezione1);
    public Mono<GenericResponseDTO<ProceduraDTO>> save (ProceduraDTO request);
}
