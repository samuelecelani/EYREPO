package it.ey.piao.api.repository;

import it.ey.entity.Funzionalita;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FunzionalitaRepository extends BaseRepository<Funzionalita, Long> {



    @Query("""
    SELECT DISTINCT f FROM Funzionalita f
    JOIN FETCH f.funzionalitaByRuoli fr
    JOIN FETCH fr.ruolo r
    WHERE r.codRuolo IN :ruoli
    """)
    List<Funzionalita> getFunzionalitaByRuolo(@Param("ruoli") List<String> ruoli);
}
