package it.ey.piao.api.repository;

import it.ey.entity.ObbiettivoPerformance;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IObbiettivoPerformanceRepository extends BaseRepository<ObbiettivoPerformance,Long> {

    @Query("SELECT op FROM ObbiettivoPerformance op WHERE op.sezione22.id = :idSezione22")
    List<ObbiettivoPerformance> findByIdSezione22(@Param("idSezione22") Long idSezione22);

    @Query("""
        SELECT op FROM ObbiettivoPerformance op
        WHERE op.tipologia = :tipologia
        AND ((:idOvp IS NULL AND op.ovp IS NULL) OR op.ovp.id = :idOvp)
        AND ((:idStrategia IS NULL AND op.ovpStrategia IS NULL) OR op.ovpStrategia.id = :idStrategia)
        """)
    List<ObbiettivoPerformance> findByTipologiaAndFilters(
        @Param("tipologia") TipologiaObbiettivo tipologia,
        @Param("idOvp") Long idOvp,
        @Param("idStrategia") Long idStrategia
    );

    @Query("""
    SELECT DISTINCT s22.piao.id
    FROM ObiettivoPerformanceStakeHolder opsh
    JOIN opsh.obbiettivoPerformance op
    JOIN op.sezione22 s22
    WHERE opsh.stakeholder.id = :stakeholderId
""")
    List<Long> findAllPiaoIdsByStakeholderInSezione22(@Param("stakeholderId") Long stakeholderId);

    /**
     * Imposta a NULL il riferimento ovp per tutti gli ObbiettivoPerformance collegati a un OVP.
     * Questo evita che gli obiettivi vengano cancellati quando si cancella un OVP.
     */
    @Modifying
    @Query("UPDATE ObbiettivoPerformance op SET op.ovp = NULL WHERE op.ovp.id = :idOvp")
    int setOvpToNullByOvpId(@Param("idOvp") Long idOvp);


    @Query("""
    SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END
    FROM ObbiettivoPerformance op
    JOIN op.sezione22 s22
    WHERE op.ovp.id = :idOvp
    AND s22.idStato IN :statiId
    """)
    boolean existsByOvpIdAndSezione22StatoIn(@Param("idOvp") Long idOvp, @Param("statiId") List<Long> statiId);


    @Query("""
    SELECT CASE WHEN COUNT(opsh) > 0 THEN true ELSE false END
    FROM ObiettivoPerformanceStakeHolder opsh
    JOIN opsh.obbiettivoPerformance op
    WHERE opsh.stakeholder.id = :stakeholderId
    AND op.sezione22.idStato IN :statiIds
    """)
    boolean existsByStakeholderIdInObiettivoPerformance(@Param("stakeholderId") Long stakeholderId, @Param("statiIds") List<Long> statiIds);

    @Query("""
        SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END
        FROM ObbiettivoPerformance op
        JOIN op.sezione22 s22
        WHERE op.ovpStrategia.id = :idOvpStrategia
        AND s22.idStato IN :statiId
    """)
    boolean existsByOvpStrategiaIdAndSezione22StatoIn(@Param("idOvpStrategia") Long idOvpStrategia, @Param("statiId") List<Long> statiId);

    @Query("""
    SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END
    FROM ObbiettivoPerformance op
    WHERE op.ovpStrategia.id = :idOvpStrategia
    """)
    boolean existsByOvpStrategiaId(@Param("idOvpStrategia") Long idOvpStrategia);

    @Modifying
    @Query("""
    UPDATE ObbiettivoPerformance op
    SET op.ovpStrategia = null
    WHERE op.ovpStrategia.id = :idOvpStrategia
    """)
    int setOvpStrategiaToNullByOvpStrategiaId(Long idOvpStrategia);



    @Modifying
    @Query("""
    UPDATE ObbiettivoPerformance op
    SET op.active = false,
        op.deactivationTime = :deactivationTime
    WHERE op.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);
}
