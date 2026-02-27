package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MotivazioneDichiarazioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMotivazioneDichiarazioneService
{
    Mono<GenericResponseDTO<MotivazioneDichiarazioneDTO>> saveOrUpdate(MotivazioneDichiarazioneDTO dto);
    Mono<Void> deleteById(Long id);
    Mono<GenericResponseDTO<List<MotivazioneDichiarazioneDTO>>> getAll();
}
