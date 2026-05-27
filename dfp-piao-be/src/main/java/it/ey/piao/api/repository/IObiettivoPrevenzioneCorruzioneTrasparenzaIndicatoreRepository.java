package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;


@Repository
public interface IObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoreRepository  extends BaseRepository<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori,Long> {

    //Soft delete delle relazioni quando viene eliminato un obiettivo (lato obettivoCorruzione)

    @Modifying
    @Query("""
        UPDATE ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori o
        SET o.active = false,
            o.deactivationTime = :deactivationTime
        WHERE o.obiettivoPrevenzioneCorruzioneTrasparenza.id = :idObiettivo
          AND o.active = true
    """)
    void softDeleteByObiettivoId(@Param("idObiettivo") Long idObiettivo,
                                 @Param("deactivationTime") LocalDateTime deactivationTime);



    //Soft delete delle relazioni quando viene eliminato un Indicatore (lato Indicatore)
    @Modifying
    @Query("""
        UPDATE ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori obi
        SET obi.active = false,
            obi.deactivationTime = :deactivationTime
        WHERE obi.indicatore.id = :idIndicatore
          AND obi.active = true
    """)
    void softDeleteByIndicatoreId(@Param("idIndicatore") Long idIndicatore,
                                  @Param("deactivationTime") LocalDateTime deactivationTime);


}
