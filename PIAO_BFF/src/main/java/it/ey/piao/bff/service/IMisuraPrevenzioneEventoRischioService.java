package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MisuraPrevenzioneDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMisuraPrevenzioneEventoRischioService {
    Mono<GenericResponseDTO<MisuraPrevenzioneEventoRischioDTO>> saveOrUpdate(MisuraPrevenzioneEventoRischioDTO misuraPrevenzioneEventoRischio);

    Mono<GenericResponseDTO<List<MisuraPrevenzioneEventoRischioDTO>>>  getMisuraPrevenzioneByEventoRischio(Long idEventoRischio);

    Mono<Void> deleteById(Long id);

    Mono<Void> deleteByEventoRischio(Long idEventoRischio);
}
