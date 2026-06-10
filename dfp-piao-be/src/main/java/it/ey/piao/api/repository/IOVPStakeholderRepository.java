package it.ey.piao.api.repository;


import it.ey.entity.OVPStakeHolder;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IOVPStakeholderRepository extends BaseRepository<OVPStakeHolder,Long> {

 //Soft delete delle relazioni quando viene eliminato un OVP (lato OVP)

    @Modifying
    @Query("""
        UPDATE OVPStakeHolder ovp
        SET ovp.active = false,
            ovp.deactivationTime = :deactivationTime
        WHERE ovp.ovp.id = :idOvp
          AND ovp.active = true
    """)
    void softDeleteByOvpId(@Param("idOvp") Long idOvp,
                           @Param("deactivationTime") LocalDateTime deactivationTime);

    //Soft delete delle relazioni quando viene eliminato uno Stakeholder (lato Stakeholder)
    @Modifying
    @Query("""
        UPDATE OVPStakeHolder sh
        SET sh.active = false,
            sh.deactivationTime = :deactivationTime
        WHERE sh.stakeholder.id = :idStakeholder
          AND sh.active = true
    """)
    void softDeleteByStakeholderId(@Param("idStakeholder") Long idStakeholder,
                                   @Param("deactivationTime") LocalDateTime deactivationTime);
}


