package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzione;
import it.ey.entity.ObiettivoPrevenzione;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IMisuraPrevenzioneRepository extends BaseRepository<MisuraPrevenzione,Long> {

   List<MisuraPrevenzione> getMisuraPrevenzioneByObiettivoPrevenzione(ObiettivoPrevenzione obiettivoPrevenzione);
}
