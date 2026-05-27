package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PromemoriaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IPromemoriaService
{
    Mono<GenericResponseDTO<List<PromemoriaDTO>>> getAll();
}
