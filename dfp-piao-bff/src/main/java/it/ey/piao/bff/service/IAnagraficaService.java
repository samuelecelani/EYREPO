package it.ey.piao.bff.service;

import it.ey.dto.AnagraficaDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAnagraficaService {
    Mono<GenericResponseDTO<List<AnagraficaDTO>>> getAll();
    Mono<GenericResponseDTO<AnagraficaDTO>> save(AnagraficaDTO anagraficaDTO);
}


