package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StoricoStatoSezioneDTO;
import reactor.core.publisher.Mono;

public interface IStoricoStatoSezioneService {

    Mono<GenericResponseDTO<StoricoStatoSezioneDTO>> save(StoricoStatoSezioneDTO dto);
}
