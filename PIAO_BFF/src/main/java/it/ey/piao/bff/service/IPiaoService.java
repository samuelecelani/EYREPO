package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IPiaoService {
    public Mono<GenericResponseDTO<PiaoDTO>> initializePiao(PiaoDTO piao);
    public Mono<GenericResponseDTO<Boolean>> redigiPiaoIsAllowed(String codPAFK);
    public Mono<GenericResponseDTO<List<PiaoDTO>>> findPiaoByCodPAFK(String codPAFK);

}
