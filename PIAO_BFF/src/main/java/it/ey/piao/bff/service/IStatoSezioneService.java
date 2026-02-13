package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StatoSezioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStatoSezioneService {
    public Mono<GenericResponseDTO<List<StatoSezioneDTO>>> getStatoSezione();

}
