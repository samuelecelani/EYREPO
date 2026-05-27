package it.ey.piao.api.service;

import it.ey.dto.AllegatoDTO;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.Sezione;

import java.util.List;

public interface IAllegatoService {
    public AllegatoDTO insertAllegato(AllegatoDTO allegato);
    public List<AllegatoDTO> getAllegatiByTipologiaFK(List<Sezione> codTipologia, List<CodTipologiaAllegato> codTipologiaAllegato, Long idPiao, boolean isDoc);
    public void deleteAllegato(Long allegatoId, boolean isDoc, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);
    List<AllegatoDTO> findByIdPiao(Long idPiao);
    AllegatoDTO findById(Long id);
    AllegatoDTO updateAllegato(AllegatoDTO allegato);
    void deleteBozzaByCodDocumento(String codDocumento);
}
