package it.ey.piao.api.repository;


import it.ey.entity.StrutturaPiao;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StrutturaPiaoRepository extends BaseRepository<StrutturaPiao, Long> {
    public StrutturaPiao findByNumeroSezione(String numeroSezione);

}

