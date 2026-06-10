package it.ey.piao.api.service;

import it.ey.dto.FaseDTO;

public interface IFaseService {

    void saveOrUpdateFase(FaseDTO fase);

    void saveOrUpdateAll(java.util.List<FaseDTO> fasi);

    void deleteFase(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);

    /**
     * Carica SOLO i dati MongoDB per la Fase (Attore e Attività)
     * @param faseDTO il DTO su cui caricare i dati MongoDB
     */
    void loadMongoDataForFase(FaseDTO faseDTO);
}
