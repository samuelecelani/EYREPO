package it.ey.piao.api.service;

import it.ey.dto.StoricoModificaDTO;
import it.ey.enums.Sezione;

import java.util.List;

public interface IStoricoModificaService {

    List<StoricoModificaDTO> findByIdSezioneAndCodTipologiaFK(Long idSezione, Sezione codTipologiaFK);

    List<StoricoModificaDTO> findByIdPiao(Long idPiao);
}
