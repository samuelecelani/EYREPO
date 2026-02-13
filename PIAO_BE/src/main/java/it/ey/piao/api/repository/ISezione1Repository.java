package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione1;
import it.ey.repository.BaseRepository;

public interface ISezione1Repository extends BaseRepository<Sezione1,Long> {
    public  Sezione1 findByPiao(Piao piao);
}
