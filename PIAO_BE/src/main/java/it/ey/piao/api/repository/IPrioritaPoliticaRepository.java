package it.ey.piao.api.repository;

import it.ey.entity.PrioritaPolitica;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPrioritaPoliticaRepository extends BaseRepository<PrioritaPolitica, Long> {
    public List<PrioritaPolitica> findBySezione1Id(Long sezioneID);;

}
