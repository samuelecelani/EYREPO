package it.ey.piao.api.repository;

import it.ey.entity.Anagrafica;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAnagraficaRepository extends BaseRepository<Anagrafica,Long> {

    @Query("SELECT a FROM Anagrafica a WHERE a.idPiao.id = :idPiao")
    Anagrafica findByIdPiao(@Param("idPiao") Long idPiao);

    @Query(value = """
        SELECT a.* FROM anagrafica a
        WHERE a.X_ACTIVE = true
          AND (:codiceIpa IS NULL OR LOWER(CAST(a.codiceIpa AS TEXT)) = LOWER(CAST(:codiceIpa AS TEXT)))
          AND (:tipologia IS NULL OR LOWER(CAST(a.tipologiaIstat AS TEXT)) = LOWER(CAST(:tipologia AS TEXT)))
          AND (:denominazione IS NULL OR (
              LOWER(CAST(a.codiceIpa AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:denominazione AS TEXT), '%'))
              OR LOWER(CAST(a.denominazioneEnte AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:denominazione AS TEXT), '%'))
          ))
        """, nativeQuery = true)
    List<Anagrafica> search(
            @Param("codiceIpa") String codiceIpa,
            @Param("tipologia") String tipologia,
            @Param("denominazione") String denominazione
    );

    @Query(value = """
        SELECT DISTINCT a.tipologiaIstat FROM anagrafica a
        WHERE a.X_ACTIVE = true
          AND a.tipologiaIstat IS NOT NULL
          AND a.tipologiaIstat <> ''
        ORDER BY a.tipologiaIstat
        """, nativeQuery = true)
    List<String> findDistinctTipologie();
}
