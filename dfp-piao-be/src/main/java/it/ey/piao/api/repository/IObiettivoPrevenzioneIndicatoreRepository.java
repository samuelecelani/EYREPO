package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPrevenzioneIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IObiettivoPrevenzioneIndicatoreRepository extends BaseRepository<ObiettivoPrevenzioneIndicatore,Long> {


    //Soft delete delle relazioni quando viene eliminato un obiettivo (lato obiettivoPrevenzione)

    @Modifying
    @Query("""
        UPDATE ObiettivoPrevenzioneIndicatore o
        SET o.active = false,
            o.deactivationTime = :deactivationTime
        WHERE o.obiettivoPrevenzione.id = :idObiettivo
          AND o.active = true
    """)
    void softDeleteByObiettivoId(@Param("idObiettivo") Long idObiettivo,
                                 @Param("deactivationTime") LocalDateTime deactivationTime);

    //Soft delete delle relazioni quando viene eliminato un Indicatore (lato Indicatore)
    @Modifying
    @Query("""
        UPDATE ObiettivoPrevenzioneIndicatore obi
        SET obi.active = false,
            obi.deactivationTime = :deactivationTime
        WHERE obi.indicatore.id = :idIndicatore
          AND obi.active = true
    """)
    void softDeleteByIndicatoreId(@Param("idIndicatore") Long idIndicatore,
                                  @Param("deactivationTime") LocalDateTime deactivationTime);


}
