package it.ey.piao.api.repository;

import it.ey.entity.Indicatore;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IIndicatoreRepository extends BaseRepository<Indicatore, Long> {
    List<Indicatore> findByPiaoIdAndCodTipologiaFK(Long piaoId, String codTipologiaFK);
}
