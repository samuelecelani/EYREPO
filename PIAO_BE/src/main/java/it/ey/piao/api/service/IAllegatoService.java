package it.ey.piao.api.service;

import it.ey.dto.AllegatoDTO;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;

import java.util.List;

public interface IAllegatoService {
    public AllegatoDTO insertAllegato(AllegatoDTO allegato);
    public List<AllegatoDTO> getAllegatiByTipologiaFK(CodTipologia codTipologia, CodTipologiaAllegato codTipologiaAllegato, Long idPiao, boolean isDoc);
    public void deleteAllegato(Long allegatoId, boolean isDoc);
}
