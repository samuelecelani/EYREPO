package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPrevenzioneCorruzioneTrasparenza;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IObiettivoPrevenzioneCorruzioneTrasparenzaRepository extends BaseRepository<ObiettivoPrevenzioneCorruzioneTrasparenza,Long> {
    List<ObiettivoPrevenzioneCorruzioneTrasparenza> getObiettivoPrevenzioneCorruzioneTrasparenzaFindBySezione23(Sezione23 sezione23);
}
