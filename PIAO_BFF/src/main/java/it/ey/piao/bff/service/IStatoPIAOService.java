package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StatoPIAODTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStatoPIAOService {
    public Mono<GenericResponseDTO<List<StatoPIAODTO>>> getStatoPIAO();
}
