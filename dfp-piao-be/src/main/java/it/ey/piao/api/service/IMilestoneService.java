package it.ey.piao.api.service;

import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;

import java.util.List;

public interface IMilestoneService
{
    void saveOrUpdate(MilestoneDTO dto);
    void deleteById(Long id, String campiModificati, Long idPiao, String testoSezione, String updatedByNameSurname, String updatedByRole, String statoSezione);
    PromemoriaDTO getPromemoriaByMilestone(Long idMilestone);
    List<MilestoneDTO> getMilestoneBySottofaseMonitoraggio(Long idSottofaseMonitoraggio);
}
