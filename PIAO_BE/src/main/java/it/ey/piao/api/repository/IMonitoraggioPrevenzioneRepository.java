package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MonitoraggioPrevenzione;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMonitoraggioPrevenzioneRepository extends BaseRepository<MonitoraggioPrevenzione, Long>
{
    List<MonitoraggioPrevenzione> getMonitoraggioByMisuraPrevenzioneEventoRischio(MisuraPrevenzioneEventoRischio misuraPrevenzioneEventoRischio);
}
