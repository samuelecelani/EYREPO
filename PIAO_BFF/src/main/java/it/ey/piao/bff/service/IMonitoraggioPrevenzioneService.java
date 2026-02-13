package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMonitoraggioPrevenzioneService
{
    Mono<GenericResponseDTO<MonitoraggioPrevenzioneDTO>> saveOrUpdate(MonitoraggioPrevenzioneDTO monitoraggioPrevenzioneDTO);

    Mono<GenericResponseDTO<List<MonitoraggioPrevenzioneDTO>>> getAllByMisuraPrevenzioneEventoRischio(Long idMisuraPrevenzioneEventoRischio);

    Mono<Void> deleteById(Long id);
}
