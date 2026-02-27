package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione31DTO;
import reactor.core.publisher.Mono;

public interface ISezione31Service
{
    Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione31DTO request);
    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id);
    Mono<GenericResponseDTO<Sezione31DTO>> getOrCreate(PiaoDTO request);
    Mono<GenericResponseDTO<Sezione31DTO>> findByPiao(Long idPiao);
}
