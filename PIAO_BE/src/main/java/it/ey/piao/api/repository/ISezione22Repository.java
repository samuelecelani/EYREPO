package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione21;
import it.ey.entity.Sezione22;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISezione22Repository extends BaseRepository<Sezione22,Long> {

    public Sezione22 findByPiao(Piao piao);
    Optional<Sezione22> findByPiaoId(Long piaoId);

}
