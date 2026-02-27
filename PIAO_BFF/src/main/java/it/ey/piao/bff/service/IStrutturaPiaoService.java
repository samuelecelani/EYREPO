package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStrutturaPiaoService {

    Mono<GenericResponseDTO<List<StrutturaPiaoDTO>>> getStrutturaPiao(Long idPiao);

    Mono<GenericResponseDTO<List<StrutturaValidazioneDTO>>> getStrutturaValidazione(Long idPiao);
}


