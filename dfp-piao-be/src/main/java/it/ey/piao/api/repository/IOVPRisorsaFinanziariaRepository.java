package it.ey.piao.api.repository;

import it.ey.entity.OVPRisorsaFinanziaria;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IOVPRisorsaFinanziariaRepository extends BaseRepository<OVPRisorsaFinanziaria, Long> {


    @Modifying
    @Query("""
        UPDATE OVPRisorsaFinanziaria r
        SET r.active = false,
            r.deactivationTime = :deactivationTime
        WHERE r.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);



    // SOFT DELETE lato cancellazione OVP
    @Modifying
    @Query("""
        UPDATE OVPRisorsaFinanziaria r
        SET r.active = false,
            r.deactivationTime = :deactivationTime
        WHERE r.ovp.id = :idOvp
          AND r.active = true
    """)
    void softDeleteByOvpId(@Param("idOvp") Long idOvp,
                           @Param("deactivationTime") LocalDateTime deactivationTime);


}
