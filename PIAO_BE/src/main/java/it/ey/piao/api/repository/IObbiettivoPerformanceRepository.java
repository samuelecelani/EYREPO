package it.ey.piao.api.repository;

import it.ey.entity.ObbiettivoPerformance;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT CASE WHEN COUNT(op) > 0 THEN true ELSE false END FROM ObbiettivoPerformance op WHERE op.ovpStrategia.id = :idStrategia")
    boolean existsByOvpStrategiaId(@Param("idStrategia") Long idStrategia);

    /**
     * Imposta a NULL il riferimento ovpStrategia per tutti gli ObbiettivoPerformance collegati a una strategia.
     * Questo evita che gli obiettivi vengano cancellati quando si cancella una strategia.
     */
    @Modifying
    @Query("UPDATE ObbiettivoPerformance op SET op.ovpStrategia = NULL WHERE op.ovpStrategia.id = :idStrategia")
    int setOvpStrategiaToNullByStrategiaId(@Param("idStrategia") Long idStrategia);

    /**
     * Imposta a NULL il riferimento ovp per tutti gli ObbiettivoPerformance collegati a un OVP.
     * Questo evita che gli obiettivi vengano cancellati quando si cancella un OVP.
     */
    @Modifying
    @Query("UPDATE ObbiettivoPerformance op SET op.ovp = NULL WHERE op.ovp.id = :idOvp")
    int setOvpToNullByOvpId(@Param("idOvp") Long idOvp);
}
