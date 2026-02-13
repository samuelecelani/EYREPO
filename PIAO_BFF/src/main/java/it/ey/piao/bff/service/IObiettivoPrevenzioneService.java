package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IObiettivoPrevenzioneService {

    Mono<GenericResponseDTO<ObiettivoPrevenzioneDTO>> saveOrUpdate(ObiettivoPrevenzioneDTO obbiettivoPrevenzione);

    Mono<GenericResponseDTO<List<ObiettivoPrevenzioneDTO>>> getAllBySezione23(Long idSezione23);

    Mono<Void> deleteById(Long id);
}
