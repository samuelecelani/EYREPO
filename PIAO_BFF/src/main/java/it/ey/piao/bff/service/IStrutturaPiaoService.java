package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StrutturaPiaoDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStrutturaPiaoService {

    public Mono<GenericResponseDTO<List<StrutturaPiaoDTO>>> getStrutturaPiao( Long idPiao);

}


