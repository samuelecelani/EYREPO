package it.ey.piao.api.service;

import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;

import java.util.List;

public interface IMilestoneService
{
    MilestoneDTO saveOrUpdate(MilestoneDTO dto);
    void deleteById(Long id);
    PromemoriaDTO getPromemoriaByMilestone(Long idMilestone);
    List<MilestoneDTO> getMilestoneBySottofaseMonitoraggio(Long idSottofaseMonitoraggio);
}
