package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Sezione1DTO;
import reactor.core.publisher.Mono;

public interface ISezione1Service {

    public Mono<GenericResponseDTO<Void>> saveOrUpdate(Sezione1DTO request);
    public Mono<GenericResponseDTO<Void>> richiediValidazione(Long id);
    public Mono<GenericResponseDTO<Sezione1DTO>> findByPiao(Long idPiao);
}
