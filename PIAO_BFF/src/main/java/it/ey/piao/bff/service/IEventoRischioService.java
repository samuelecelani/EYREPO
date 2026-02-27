package it.ey.piao.bff.service;

import it.ey.dto.EventoRischioDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IEventoRischioService {

    Mono<GenericResponseDTO<EventoRischioDTO>> saveOrUpdate(EventoRischioDTO eventoRischioDTO);

    Mono<GenericResponseDTO<List<EventoRischioDTO>>> getAllByAttivitaSensibile(Long idAttivitaSensibile);

    Mono<Void> deleteById(Long id);

    Mono<Void> deleteByAttivitaSensibile(Long idAttivitaSensibile);
}
