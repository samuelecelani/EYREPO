package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Sezione1DTO;
import reactor.core.publisher.Mono;

public interface ISezione1Service {

    public Mono<GenericResponseDTO<Sezione1DTO>> saveOrUpdate(Sezione1DTO request);
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id);
}
