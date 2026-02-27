package it.ey.piao.api.repository;

import it.ey.entity.CategoriaObiettivi;
import it.ey.entity.Sezione4;
import it.ey.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICategoriaObiettiviRepository extends BaseRepository<CategoriaObiettivi, Long> {
    List<CategoriaObiettivi> getCategoriaObiettiviBySezione4(Sezione4 sezione4);
}
