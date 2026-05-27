package it.ey.piao.api.repository;

import it.ey.entity.DatiPubblicati;
import it.ey.entity.ObbligoLegge;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IDatiPubblicatiRepository extends BaseRepository<DatiPubblicati,Long> {

    @Query("SELECT d FROM DatiPubblicati d WHERE d.obbligoLegge.id = :idObbligoLegge ")
    List<DatiPubblicati> findByIdObbligoLegge(@Param("idObbligoLegge") Long idObbligoLegge);

    @Modifying
    @Query("UPDATE DatiPubblicati d SET d.active = false, d.deactivationTime = :deactivationTime WHERE d.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);



    @Modifying
    @Query("""
        UPDATE DatiPubblicati dp
        SET dp.active = false,
            dp.deactivationTime = :deactivationTime
        WHERE dp.obbligoLegge.id = :idObbligoLegge
          AND dp.active = true
    """)
    void softDeleteByObbligoLeggeId(@Param("idObbligoLegge") Long idObbligoLegge,
                                    @Param("deactivationTime") LocalDateTime deactivationTime);

}
