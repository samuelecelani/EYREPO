package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione21;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ISezione21Repository extends BaseRepository<Sezione21,Long> {
    public Sezione21 findByPiao(Piao piao);
    Optional<Sezione21> findByPiaoId(Long piaoId);

}
