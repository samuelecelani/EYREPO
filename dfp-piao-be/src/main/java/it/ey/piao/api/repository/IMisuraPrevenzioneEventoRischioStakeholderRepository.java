package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MisuraPrevenzioneEventoRischioStakeholder;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IMisuraPrevenzioneEventoRischioStakeholderRepository extends BaseRepository<MisuraPrevenzioneEventoRischioStakeholder,Long> {


    // SOFT DELETE eliminazione relazione
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneEventoRischioStakeholder mis
        SET mis.active = false,
            mis.deactivationTime = :deactivationTime
        WHERE mis.misuraPrevenzioneEventoRischio.id = :idMisuraPrevenzioneEventoRischio
          AND mis.active = true
    """)
    void softDeleteByMisuraPrevenzioneEventoRischioId(@Param("idMisuraPrevenzioneEventoRischio") Long idMisuraPrevenzioneEventoRischio,
                                                                 @Param("deactivationTime") LocalDateTime deactivationTime);



    // SOFT DELETE eliminazione relazione con idStakeholder
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneEventoRischioStakeholder mis
        SET mis.active = false,
            mis.deactivationTime = :deactivationTime
        WHERE mis.stakeholder.id = :idStakeholder
          AND mis.active = true
    """)
    void softDeleteByStakeholderId(@Param("idStakeholder") Long idStakeholder,
                                                                 @Param("deactivationTime") LocalDateTime deactivationTime);

}
