package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.IndicatoreDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IIndicatoreService {

    Mono<GenericResponseDTO<IndicatoreDTO>> saveOrUpdate(IndicatoreDTO dto);
    Mono<GenericResponseDTO<List<IndicatoreDTO>>> findBy(Long idPiao, Long idEntitaFK, String codTipologiaFK);
}
