package it.ey.piao.api.repository;

import it.ey.entity.ObiettivoPrevenzioneCorruzioneTrasparenza;
import it.ey.entity.Sezione23;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IObiettivoPrevenzioneCorruzioneTrasparenzaRepository extends BaseRepository<ObiettivoPrevenzioneCorruzioneTrasparenza,Long> {
    List<ObiettivoPrevenzioneCorruzioneTrasparenza> getObiettivoPrevenzioneCorruzioneTrasparenzaFindBySezione23(Sezione23 sezione23);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM ObiettivoPrevenzioneCorruzioneTrasparenza o WHERE o.ovpStrategia.id = :idStrategia")
    boolean existsByOvpStrategiaId(@Param("idStrategia") Long idStrategia);

    /**
     * Imposta a NULL il riferimento ovpStrategia per tutti gli ObiettivoPrevenzioneCorruzioneTrasparenza collegati a una strategia.
     * Questo evita che gli obiettivi vengano cancellati quando si cancella una strategia.
     */
    @Modifying
    @Query("UPDATE ObiettivoPrevenzioneCorruzioneTrasparenza o SET o.ovpStrategia = NULL WHERE o.ovpStrategia.id = :idStrategia")
    int setOvpStrategiaToNullByStrategiaId(@Param("idStrategia") Long idStrategia);

    /**
     * Imposta a NULL il riferimento ovp per tutti gli ObiettivoPrevenzioneCorruzioneTrasparenza collegati a un OVP.
     * Questo evita che gli obiettivi vengano cancellati quando si cancella un OVP.
     */
    @Modifying
    @Query("UPDATE ObiettivoPrevenzioneCorruzioneTrasparenza o SET o.ovp = NULL WHERE o.ovp.id = :idOvp")
    int setOvpToNullByOvpId(@Param("idOvp") Long idOvp);

    /**
     * Imposta a NULL il riferimento obbiettivoPerformance per tutti gli ObiettivoPrevenzioneCorruzioneTrasparenza collegati a un ObbiettivoPerformance.
     * Questo evita che gli obiettivi prevenzione vengano cancellati quando si cancella un ObbiettivoPerformance.
     */
    @Modifying
    @Query("UPDATE ObiettivoPrevenzioneCorruzioneTrasparenza o SET o.obbiettivoPerformance = NULL WHERE o.obbiettivoPerformance.id = :idObbiettivoPerformance")
    int setObbiettivoPerformanceToNullByObbiettivoPerformanceId(@Param("idObbiettivoPerformance") Long idObbiettivoPerformance);


    @Query("""
    SELECT CASE WHEN COUNT(opct) > 0 THEN true ELSE false END
    FROM ObiettivoPrevenzioneCorruzioneTrasparenza opct
    WHERE opct.ovp.id = :idOvp
    AND opct.sezione23.idStato IN :statiId
    """)
    boolean existsByOvpIdInSezione23(
        @Param("idOvp") Long idOvp,
        @Param("statiId") List<Long> statiId);



    @Query("""
    SELECT CASE WHEN COUNT(opct) > 0 THEN true ELSE false END
    FROM ObiettivoPrevenzioneCorruzioneTrasparenza opct
    WHERE opct.obbiettivoPerformance.id = :idObbPerf
    AND opct.sezione23.idStato IN :statiId
    """)
    boolean existsByObbPerfIdInSezione23(@Param("idObbPerf") Long idObbPerf, @Param("statiId") List<Long> statiId);


    @Query("""
    SELECT CASE WHEN COUNT(opct) > 0 THEN true ELSE false END
    FROM ObiettivoPrevenzioneCorruzioneTrasparenza opct
    WHERE opct.ovpStrategia.id  = :idOvpStrategia
    AND opct.sezione23.idStato IN :statiId
    """)
    boolean existsByOvpStrategiaInSezione23(@Param("idOvpStrategia") Long idOvpStrategia, @Param("statiId") List<Long> statiId);




    @Modifying
    @Query("""
    UPDATE ObiettivoPrevenzioneCorruzioneTrasparenza opct
    SET opct.ovpStrategia = null
    WHERE opct.ovpStrategia.id = :idOvpStrategia
""")
    int setOvpStrategiaToNullByOvpStrategiaId(Long idOvpStrategia);

    @Modifying
    @Query("UPDATE ObiettivoPrevenzioneCorruzioneTrasparenza o SET o.active = false, o.deactivationTime = :deactivationTime WHERE o.id = :id")
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
