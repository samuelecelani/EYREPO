package it.ey.piao.api.repository;

import it.ey.entity.Fase;
import it.ey.entity.Sezione22;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFaseRepository extends BaseRepository<Fase, Long> {

public List<Fase> getFaseBySezione22(Sezione22 sezione22) ;
}
