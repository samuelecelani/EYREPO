package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IOVPService {
    public Mono<GenericResponseDTO<OVPDTO>> saveOrUpdate(OVPDTO request);
    public Mono<GenericResponseDTO<List<OVPDTO>>> getOvpByIdSezione21(Long idSezione21);
    public Mono<GenericResponseDTO<Void>> delete(Long id);
}
