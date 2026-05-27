package it.ey.piao.bff.service;

import it.ey.dto.AmministrazioneInternalDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAmministrazioneService {

    Mono<GenericResponseDTO<List<AmministrazioneInternalDTO>>> search(
            String codiceIpa,
            String tipologia,
            String denominazione
    );

    Mono<GenericResponseDTO<List<String>>> getTipologie();
}
