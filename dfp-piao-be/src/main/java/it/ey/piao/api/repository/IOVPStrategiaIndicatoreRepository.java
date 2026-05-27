package it.ey.piao.api.repository;

import it.ey.entity.OVPStrategia;
import it.ey.entity.OVPStrategiaIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IOVPStrategiaIndicatoreRepository extends BaseRepository<OVPStrategiaIndicatore, Long> {
    // Trova tutti gli indicatori per una specifica strategia
    List<OVPStrategiaIndicatore> findByOvpStrategia(OVPStrategia ovpStrategia);

    // Elimina tutti gli indicatori per una specifica strategia
    void deleteByOvpStrategia(OVPStrategia ovpStrategia);



      //Soft delete delle relazioni quando viene eliminata una OVPStrategia (lato OVPStrategia)
    @Modifying
    @Query("""
        UPDATE OVPStrategiaIndicatore rel
        SET rel.active = false,
            rel.deactivationTime = :deactivationTime
        WHERE rel.ovpStrategia.id = :idOvpStrategia
          AND rel.active = true
    """)
    void softDeleteByOvpStrategiaId(@Param("idOvpStrategia") Long idOvpStrategia,
                                    @Param("deactivationTime") LocalDateTime deactivationTime);

     // Soft delete delle relazioni quando viene eliminato un Indicatore (lato Indicatore)
    @Modifying
    @Query("""
        UPDATE OVPStrategiaIndicatore rel
        SET rel.active = false,
            rel.deactivationTime = :deactivationTime
        WHERE rel.indicatore.id = :idIndicatore
          AND rel.active = true
    """)
    void softDeleteByIndicatoreId(@Param("idIndicatore") Long idIndicatore,
                                  @Param("deactivationTime") LocalDateTime deactivationTime);
}


