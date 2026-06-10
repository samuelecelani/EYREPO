package it.ey.sync.service;

import it.ey.sync.dto.AmministrazioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface AmministrazioneService {

    List<AmministrazioneDTO> findAll();

    Optional<AmministrazioneDTO> findByCodiceIpa(String codiceIpa);

    AmministrazioneDTO save(AmministrazioneDTO dto);

    void deleteByCodiceIpa(String codiceIpa);

    Mono<List<AmministrazioneDTO>> getAmministrazioni();

    void syncAll(List<AmministrazioneDTO> list);
}

