package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MisuraPrevenzioneDTO;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMisuraPrevenzioneService {
    Mono<GenericResponseDTO<MisuraPrevenzioneDTO>> saveOrUpdate(MisuraPrevenzioneDTO misuraPrevenzione);

    Mono<GenericResponseDTO<List<MisuraPrevenzioneDTO>>>  getMisuraPrevenzioneByObiettivoPrevenzione(Long idObiettivoPrevenzione);

    Mono <GenericResponseDTO<List<MisuraPrevenzioneDTO>>> getAllBySezione23(Long idSezione23);
    Mono<Void> deleteById(Long id);
}
