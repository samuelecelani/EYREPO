package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MotivazioneDichiarazioneDTO;
import reactor.core.publisher.Mono;

public interface IMotivazioneDichiarazioneService
{
    Mono<GenericResponseDTO<MotivazioneDichiarazioneDTO>> saveOrUpdate(MotivazioneDichiarazioneDTO dto);
    Mono<Void> deleteById(Long id);
}
