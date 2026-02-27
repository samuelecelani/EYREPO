package it.ey.piao.bff.service;

import it.ey.dto.ConfigFeInitializerDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IConfigInitializerService {

    public Mono<GenericResponseDTO<ConfigFeInitializerDTO>> configInitializer();
}
