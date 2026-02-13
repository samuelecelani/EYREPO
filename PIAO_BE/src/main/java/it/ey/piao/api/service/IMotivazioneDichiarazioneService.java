package it.ey.piao.api.service;

import it.ey.dto.MotivazioneDichiarazioneDTO;

public interface IMotivazioneDichiarazioneService
{
    MotivazioneDichiarazioneDTO saveOrUpdate(MotivazioneDichiarazioneDTO dto);
    void delete(Long id);
}
