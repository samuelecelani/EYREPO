package it.ey.piao.api.repository;

import it.ey.entity.ObbligoLegge;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IObbligoLeggeRepository extends BaseRepository<ObbligoLegge,Long> {
    List<ObbligoLegge> getObiettivoPrevenzioneBySezione23(Sezione23 sezione23);
}
