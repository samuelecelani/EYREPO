package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPerformanceStakeHolder;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IObiettivoPerformanceStakeHolderRepository extends BaseRepository <ObiettivoPerformanceStakeHolder,Long> {

    //Soft delete delle relazioni quando viene eliminato un obiettivo (lato obettivoPerformance)

    @Modifying
    @Query("""
        UPDATE ObiettivoPerformanceStakeHolder obb
        SET obb.active = false,
            obb.deactivationTime = :deactivationTime
        WHERE obb.obbiettivoPerformance.id = :idObiettivo
          AND obb.active = true
    """)
    void softDeleteByObiettivoId(@Param("idObiettivo") Long idObiettivo,
                           @Param("deactivationTime") LocalDateTime deactivationTime);

    //Soft delete delle relazioni quando viene eliminato uno Stakeholder (lato Stakeholder)
    @Modifying
    @Query("""
        UPDATE ObiettivoPerformanceStakeHolder obbsh
        SET obbsh.active = false,
            obbsh.deactivationTime = :deactivationTime
        WHERE obbsh.stakeholder.id = :idStakeholder
          AND obbsh.active = true
    """)
    void softDeleteByStakeholderId(@Param("idStakeholder") Long idStakeholder,
                                   @Param("deactivationTime") LocalDateTime deactivationTime);
}
