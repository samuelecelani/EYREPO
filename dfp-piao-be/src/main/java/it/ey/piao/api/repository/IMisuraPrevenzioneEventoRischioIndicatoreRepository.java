package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzioneEventoRischioIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface IMisuraPrevenzioneEventoRischioIndicatoreRepository extends BaseRepository<MisuraPrevenzioneEventoRischioIndicatore,Long> {


    // SOFT DELETE eliminazione relazione
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneEventoRischioIndicatore mis
        SET mis.active = false,
            mis.deactivationTime = :deactivationTime
        WHERE mis.misuraPrevenzioneEventoRischio.id = :idMisuraPrevenzioneEventoRischio
          AND mis.active = true
    """)
    void softDeleteByMisuraPrevenzioneEventoRischioId(@Param("idMisuraPrevenzioneEventoRischio") Long idMisuraPrevenzioneEventoRischio,
                                                      @Param("deactivationTime") LocalDateTime deactivationTime);



    // SOFT DELETE eliminazione relazione con idIndicatore
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneEventoRischioIndicatore mis
        SET mis.active = false,
            mis.deactivationTime = :deactivationTime
        WHERE mis.indicatore.id = :idIndicatore
          AND mis.active = true
    """)
    void softDeleteByIndicatoreId(@Param("idIndicatore") Long idIndicatore,
                                   @Param("deactivationTime") LocalDateTime deactivationTime);





}
