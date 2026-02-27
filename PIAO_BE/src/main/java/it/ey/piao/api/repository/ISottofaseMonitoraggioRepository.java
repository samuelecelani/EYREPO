package it.ey.piao.api.repository;

import it.ey.entity.Sezione4;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISottofaseMonitoraggioRepository extends BaseRepository<SottofaseMonitoraggio, Long> {
    List<SottofaseMonitoraggio> getSottofaseMonitoraggioBySezione4(Sezione4 sezione4);
}
