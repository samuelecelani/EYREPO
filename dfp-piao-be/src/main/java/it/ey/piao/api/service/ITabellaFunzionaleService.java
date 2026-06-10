package it.ey.piao.api.service;

import it.ey.dto.TabellaFunzionaleDTO;

import java.util.List;

public interface ITabellaFunzionaleService {
    List<TabellaFunzionaleDTO> findByIdEntitaFKAndCodTipologiaFK(Long idEntitaFK, String codTipologiaFK);
    TabellaFunzionaleDTO save(TabellaFunzionaleDTO tabellaFunzionale);
    void deleteById(Long id, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);
}
