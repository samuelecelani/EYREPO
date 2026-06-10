package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzioneStakeholder;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IMisuraPrevenzioneStakeholderRepository extends BaseRepository<MisuraPrevenzioneStakeholder,Long> {



    //Soft delete delle relazioni quando viene eliminato una misiura (lato misuraPrevenzione)

    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneStakeholder ms
        SET ms.active = false,
            ms.deactivationTime = :deactivationTime
        WHERE ms.misuraPrevenzione.id = :idMisura
          AND ms.active = true
    """)
    void softDeleteByMisuraId(@Param("idMisura") Long idMisura,
                                 @Param("deactivationTime") LocalDateTime deactivationTime);

    //Soft delete delle relazioni quando viene eliminato uno Stakeholder (lato Stakeholder)
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneStakeholder ms
        SET ms.active = false,
            ms.deactivationTime = :deactivationTime
        WHERE ms.stakeholder.id = :idStakeholder
          AND ms.active = true
    """)
    void softDeleteByStakeholderId(@Param("idStakeholder") Long idStakeholder,
                                   @Param("deactivationTime") LocalDateTime deactivationTime);
}
