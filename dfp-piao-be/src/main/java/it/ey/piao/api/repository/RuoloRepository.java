package it.ey.piao.api.repository;

import it.ey.entity.Ruolo;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RuoloRepository extends BaseRepository<Ruolo, Long> {

    @Query("SELECT r FROM Ruolo r WHERE r.tipologia IN :tipologia")
    List<Ruolo> findByTipologia(@Param("tipologia") List<String> tipologia);

    Ruolo findByCodRuolo(String codRuolo);
}
