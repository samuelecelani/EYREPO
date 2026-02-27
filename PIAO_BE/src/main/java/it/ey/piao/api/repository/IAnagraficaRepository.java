package it.ey.piao.api.repository;

import it.ey.entity.Anagrafica;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAnagraficaRepository extends BaseRepository<Anagrafica,Long> {
}
