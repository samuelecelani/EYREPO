package it.ey.piao.api.repository;

import it.ey.entity.PrioritaPolitica;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPrioritaPoliticaRepository extends BaseRepository<PrioritaPolitica, Long> {

    /**
     * Trova tutte le PrioritaPolitica associate a una specifica Sezione1.
     * Query ottimizzata con JOIN FETCH per caricare le relazioni necessarie.
     */
    @Query("""
        SELECT DISTINCT p FROM PrioritaPolitica p
        LEFT JOIN FETCH p.sezione1 s1
        WHERE p.sezione1.id = :sezioneId
        """)
    List<PrioritaPolitica> findBySezione1Id(@Param("sezioneId") Long sezioneId);

    /**
     * Trova tutte le PrioritaPolitica associate a un PIAO attraverso Sezione1.
     * Query ottimizzata con JOIN FETCH per caricare tutte le relazioni necessarie.
     */
    @Query("""
        SELECT DISTINCT p FROM PrioritaPolitica p
        LEFT JOIN FETCH p.sezione1 s1
        LEFT JOIN FETCH s1.piao piao
        WHERE s1.piao.id = :piaoId
        """)
    List<PrioritaPolitica> findByPiaoId(@Param("piaoId") Long piaoId);

}
