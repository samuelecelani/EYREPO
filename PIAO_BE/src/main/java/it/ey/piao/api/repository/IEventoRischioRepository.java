package it.ey.piao.api.repository;

import it.ey.entity.AttivitaSensibile;
import it.ey.entity.EventoRischio;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEventoRischioRepository extends BaseRepository<EventoRischio, Long> {

    List<EventoRischio> findByAttivitaSensibile(AttivitaSensibile attivitaSensibile);

    List<EventoRischio> findByAttivitaSensibileId(Long idAttivitaSensibile);

    void deleteByAttivitaSensibileId(Long idAttivitaSensibile);
}
