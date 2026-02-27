package it.ey.piao.api.repository;

import it.ey.entity.AreaOrganizzativa;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAreaOrganizzativaRepository extends BaseRepository<AreaOrganizzativa, Long> {

    /**
     * Trova tutte le AreaOrganizzativa associate a una specifica Sezione1.
     * Query ottimizzata con JOIN FETCH per caricare le relazioni necessarie.
     */
    @Query("""
        SELECT DISTINCT a FROM AreaOrganizzativa a
        LEFT JOIN FETCH a.sezione1 s1
        WHERE a.sezione1.id = :sezioneId
        """)
    List<AreaOrganizzativa> findBySezione1Id(@Param("sezioneId") Long sezioneId);

    /**
     * Trova tutte le AreaOrganizzativa associate a un PIAO attraverso Sezione1.
     * Query ottimizzata con JOIN FETCH per caricare tutte le relazioni necessarie.
     */
    @Query("""
        SELECT DISTINCT a FROM AreaOrganizzativa a
        LEFT JOIN FETCH a.sezione1 s1
        LEFT JOIN FETCH s1.piao piao
        WHERE s1.piao.id = :piaoId
        """)
    List<AreaOrganizzativa> findByPiaoId(@Param("piaoId") Long piaoId);

}
