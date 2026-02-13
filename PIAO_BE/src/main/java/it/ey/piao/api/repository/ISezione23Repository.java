package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione22;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ISezione23Repository extends BaseRepository<Sezione23,Long> {

    public Sezione23 findByPiao(Piao piao);
}
