package it.ey.piao.api.service;

import it.ey.dto.FaseDTO;

public interface IFaseService {

    FaseDTO saveOrUpdateFase(FaseDTO fase);

    void deleteFase(Long id);

    /**
     * Carica SOLO i dati MongoDB per la Fase (Attore e Attività)
     * @param faseDTO il DTO su cui caricare i dati MongoDB
     */
    void loadMongoDataForFase(FaseDTO faseDTO);
}
