package it.ey.piao.api.repository;

import it.ey.entity.Procedura;
import it.ey.entity.Sezione21;
import it.ey.repository.BaseRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IProceduraRepository extends BaseRepository<Procedura, Long> {

 public List<Procedura> getProcedureBySezione21(Sezione21 sezione21);
}
