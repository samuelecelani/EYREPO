package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.entity.Sezione4;
import it.ey.repository.BaseRepository;

public interface ISezione4Repository extends BaseRepository<Sezione4, Long> {
    Sezione4 findByPiao(Piao piao);
}
