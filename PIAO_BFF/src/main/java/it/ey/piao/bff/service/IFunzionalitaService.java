package it.ey.piao.bff.service;

import it.ey.dto.FunzionalitaDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IFunzionalitaService {
    public Mono<GenericResponseDTO<List<FunzionalitaDTO>>> getFunzionalitàByRuolo( List<String> ruoli);
}
