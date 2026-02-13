package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPrevenzione;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IObiettivoPrevenzioneRepository extends BaseRepository<ObiettivoPrevenzione,Long> {


    List<ObiettivoPrevenzione> getObiettivoPrevenzioneBySezione23(Sezione23 sezione23);
}
