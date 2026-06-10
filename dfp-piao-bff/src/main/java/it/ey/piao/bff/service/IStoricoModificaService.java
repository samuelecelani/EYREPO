package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StoricoModificaDTO;
import it.ey.enums.Sezione;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStoricoModificaService {

    Mono<GenericResponseDTO<List<StoricoModificaDTO>>> findByIdSezioneAndCodTipologiaFK(Long idSezione, Sezione codTipologiaFK);

    Mono<GenericResponseDTO<List<StoricoModificaDTO>>> findByIdPiao(Long idPiao);
}
