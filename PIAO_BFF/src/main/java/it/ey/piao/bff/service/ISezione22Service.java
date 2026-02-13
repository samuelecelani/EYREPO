package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione22DTO;
import reactor.core.publisher.Mono;

public interface ISezione22Service {
    Mono<GenericResponseDTO<Sezione22DTO>> saveOrUpdate(Sezione22DTO request);
    Mono<GenericResponseDTO<Void>> richiediValidazione(Long id);
    Mono<GenericResponseDTO<Sezione22DTO>> getOrCreate(PiaoDTO request);
}

