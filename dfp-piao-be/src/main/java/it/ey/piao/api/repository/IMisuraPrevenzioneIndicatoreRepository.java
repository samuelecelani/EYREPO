package it.ey.piao.api.repository;

import it.ey.entity.MisuraPrevenzioneIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IMisuraPrevenzioneIndicatoreRepository extends BaseRepository<MisuraPrevenzioneIndicatore,Long> {


    //Soft delete delle relazioni quando viene eliminata una misura(lato misuraPrevenzione)

    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneIndicatore mi
        SET mi.active = false,
            mi.deactivationTime = :deactivationTime
        WHERE mi.misuraPrevenzione.id = :idMisura
          AND mi.active = true
    """)
    void softDeleteByMisuraId(@Param("idMisura") Long idMisura,
                                 @Param("deactivationTime") LocalDateTime deactivationTime);

    //Soft delete delle relazioni quando viene eliminato un Indicatore (lato Indicatore)
    @Modifying
    @Query("""
        UPDATE MisuraPrevenzioneIndicatore mi
        SET mi.active = false,
            mi.deactivationTime = :deactivationTime
        WHERE mi.indicatore.id = :idIndicatore
          AND mi.active = true
    """)
    void softDeleteByIndicatoreId(@Param("idIndicatore") Long idIndicatore,
                                  @Param("deactivationTime") LocalDateTime deactivationTime);
}
