package it.ey.piao.api.repository;

import it.ey.entity.ObbiettivoPerformance;
import it.ey.entity.ObiettivoPerformanceIndicatore;
import it.ey.entity.Sezione22;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IObbiettivoPerformanceRepository extends BaseRepository<ObbiettivoPerformance,Long> {

    List<ObbiettivoPerformance> findBySezione22(Sezione22 sezione22);
}
