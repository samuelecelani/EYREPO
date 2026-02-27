package it.ey.piao.api.repository;

import it.ey.entity.Piao;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PiaoRepository extends BaseRepository<Piao, Long> {

    @Query("""
            SELECT p FROM Piao p
            WHERE p.codPAFK = :codPAFK
              AND p.createdTs BETWEEN :startDate AND :endDate
              AND p.idStato <> :idStato
            ORDER BY p.versione DESC
    """)
    public Piao findPiaoByMancataDichiarazione(
        @Param("codPAFK")String codPAFK,
        @Param("startDate")LocalDate startDate,
        @Param("endDate")LocalDate endDate,
        @Param("idStato")Long idStato
    );

    // Recupera l'ultimo PIAO per PA e anno corrente
   public Piao findTopByCodPAFKAndCreatedTsBetweenOrderByVersioneDesc(
        String codPAFK,
        LocalDate startDate,
        LocalDate endDate
    );

   public List<Piao> findByCodPAFK(String codPAF);
    public List<Piao> findByCodPAFKOrderByCreatedTsDesc(String codPAFK);


    /**
     * Query nativa ottimizzata che recupera tutte le informazioni delle sezioni in una sola query.
     * Ritorna una lista di array Object[] con: [numeroSezione, sezioneId, updatedTs]
     */
    @Query(value = """
        SELECT '1' as numeroSezione, s1.id as sezioneId, s1.x_updated_ts as updatedTs
        FROM sezione1 s1
        WHERE s1.idPiao = :piaoId
        UNION ALL
        SELECT '21', s21.id, s21.x_updated_ts
        FROM sezione21 s21
        WHERE s21.idPiao = :piaoId
        UNION ALL
        SELECT '22', s22.id, s22.x_updated_ts
        FROM sezione22 s22
        WHERE s22.idPiao = :piaoId
        UNION ALL
        SELECT '23', s23.id, s23.x_updated_ts
        FROM sezione23 s23
        WHERE s23.idPiao = :piaoId
    """, nativeQuery = true)
    List<Object[]> findAllSezioniInfoByPiaoId(@Param("piaoId") Long piaoId);
}
