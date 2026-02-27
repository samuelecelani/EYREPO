package it.ey.piao.bff.service;

import it.ey.dto.EventoRischioDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IMilestoneService
{
    Mono<GenericResponseDTO<MilestoneDTO>> saveOrUpdate(MilestoneDTO dto);

    Mono<GenericResponseDTO<List<PromemoriaDTO>>> getPromemoriaByMilestone(Long id);

    Mono<GenericResponseDTO<List<MilestoneDTO>>> getMilestoneBySottofaseMonitoraggio(Long idSottofaseMonitoraggio);

    Mono<Void> deleteById(Long id);
}
