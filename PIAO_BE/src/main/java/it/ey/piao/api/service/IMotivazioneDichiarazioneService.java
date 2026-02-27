package it.ey.piao.api.service;

import it.ey.dto.MotivazioneDichiarazioneDTO;

import java.util.List;

public interface IMotivazioneDichiarazioneService
{
    MotivazioneDichiarazioneDTO saveOrUpdate(MotivazioneDichiarazioneDTO dto);
    void delete(Long id);
    List<MotivazioneDichiarazioneDTO> findAll();
}
