package it.ey.piao.api.repository;

import it.ey.entity.AreaOrganizzativa;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAreaOrganizzativaRepository extends BaseRepository<AreaOrganizzativa, Long> {
    public List<AreaOrganizzativa> findBySezione1Id(Long sezioneID);


}
