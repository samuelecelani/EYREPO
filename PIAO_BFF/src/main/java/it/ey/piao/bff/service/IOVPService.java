package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ItemMatriceDTO;
import it.ey.dto.OVPDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IOVPService {
    Mono<GenericResponseDTO<OVPDTO>> saveOrUpdate(OVPDTO request);
    Mono<GenericResponseDTO<List<OVPDTO>>> getOvpByIdSezione21(Long idSezione21);
    Mono<GenericResponseDTO<List<OVPDTO>>> getOvpByPiaoId(Long piaoId);
    Mono<GenericResponseDTO<Void>> delete(Long id);
    Mono<GenericResponseDTO<List<ItemMatriceDTO>>> getOvpMatriceByIdSezione21(Long idSezione21, Long idSezione1, Long idPiao);
}
