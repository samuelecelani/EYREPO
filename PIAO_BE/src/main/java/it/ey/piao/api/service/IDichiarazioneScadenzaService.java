package it.ey.piao.api.service;

import it.ey.dto.DichiarazioneScadenzaDTO;

public interface IDichiarazioneScadenzaService
{
    DichiarazioneScadenzaDTO saveOrUpdate(DichiarazioneScadenzaDTO dto);
    void delete(Long id);
    DichiarazioneScadenzaDTO getExistingDichiarazioneScadenza(String codPAFK);
}
