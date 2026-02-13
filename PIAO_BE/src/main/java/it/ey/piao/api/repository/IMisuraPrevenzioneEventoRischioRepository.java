package it.ey.piao.api.repository;

import it.ey.entity.EventoRischio;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMisuraPrevenzioneEventoRischioRepository extends BaseRepository<MisuraPrevenzioneEventoRischio,Long> {

   List<MisuraPrevenzioneEventoRischio> getMisuraPrevenzioneByEventoRischio(EventoRischio eventoRischio);

}
