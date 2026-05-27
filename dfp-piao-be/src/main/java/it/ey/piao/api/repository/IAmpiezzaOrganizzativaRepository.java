package it.ey.piao.api.repository;

import it.ey.entity.AmpiezzaOrganizzativa;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAmpiezzaOrganizzativaRepository extends BaseRepository<AmpiezzaOrganizzativa, Long> {

    /**
     * Trova tutte le AmpiezzaOrganizzativa associate a una specifica Sezione31.
     */
    @Query("""
        SELECT DISTINCT a FROM AmpiezzaOrganizzativa a
        LEFT JOIN FETCH a.sezione31 s31
        WHERE a.sezione31.id = :sezioneId
        """)
    List<AmpiezzaOrganizzativa> findBySezione31Id(@Param("sezioneId") Long sezioneId);

    @Modifying
    @Query("UPDATE AmpiezzaOrganizzativa a SET a.active = false, a.deactivationTime = :deactivationTime WHERE a.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

}
