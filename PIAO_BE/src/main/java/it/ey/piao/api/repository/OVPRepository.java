package it.ey.piao.api.repository;

import it.ey.entity.OVP;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OVPRepository extends BaseRepository<OVP, Long> {

    /**
     * Trova tutti gli OVP associati a una specifica Sezione21.
     * Query semplice senza JOIN FETCH per performance ottimali.
     */
    @Query("""
        SELECT DISTINCT o FROM OVP o
        LEFT JOIN o.sezione21 s21
        WHERE o.sezione21.id = :idSezione21
        ORDER BY o.id
        """)
    List<OVP> findBySezione21Id(@Param("idSezione21") Long idSezione21);

    /**
     * Trova tutti gli OVP associati a un PIAO attraverso Sezione21.
     * Query semplice senza JOIN FETCH per performance ottimali.
     */
    @Query("""
        SELECT DISTINCT o FROM OVP o
        LEFT JOIN o.sezione21 s21
        LEFT JOIN s21.piao p
        WHERE s21.piao.id = :piaoId
        """)
    List<OVP> findByPiaoId(@Param("piaoId") Long piaoId);


}
