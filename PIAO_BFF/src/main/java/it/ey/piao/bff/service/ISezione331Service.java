package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione331DTO;
import reactor.core.publisher.Mono;

public interface ISezione331Service {
    Mono<GenericResponseDTO<Sezione331DTO>> getOrCreate(PiaoDTO request);

    Mono<GenericResponseDTO<Sezione331DTO>> saveOrUpdate(Sezione331DTO request);

    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id);

    Mono<GenericResponseDTO<Sezione331DTO>> findByPiao(Long idPiao);

}
