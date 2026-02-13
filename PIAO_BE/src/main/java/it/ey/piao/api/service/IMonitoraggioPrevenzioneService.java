package it.ey.piao.api.service;

import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.entity.MisuraPrevenzione;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MonitoraggioPrevenzione;

import java.util.List;

public interface IMonitoraggioPrevenzioneService
{
    MonitoraggioPrevenzioneDTO saveOrUpdate(MonitoraggioPrevenzioneDTO monitoraggioPrevenzioneDTO);

    void deleteById(Long id);

    List<MonitoraggioPrevenzioneDTO> getAllByMisuraPrevenzioneEventoRischio(Long idMisuraPrevenzioneEventoRischio);
}
