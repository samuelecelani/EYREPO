package it.ey.piao.api.repository;

import it.ey.entity.OVPPrioritaPolitica;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IOVPPrioritaPoliticaRepository extends BaseRepository<OVPPrioritaPolitica, Long> {

    //Soft delete delle relazioni quando viene eliminato un OVp (lato OVP)

    @Modifying
    @Query("""
        UPDATE OVPPrioritaPolitica rel
        SET rel.active = false,
            rel.deactivationTime = :deactivationTime
        WHERE rel.ovp.id = :idOvp
          AND rel.active = true
    """)
    void softDeleteByOvpId(@Param("idOvp") Long idOvp,
                           @Param("deactivationTime") LocalDateTime deactivationTime);


    // Soft delete delle relazioni quando viene eliminatauna Priorità Politica (lato PrioritaPolitica)

    @Modifying
    @Query("""
        UPDATE OVPPrioritaPolitica rel
        SET rel.active = false,
            rel.deactivationTime = :deactivationTime
        WHERE rel.prioritaPolitica.id = :idPrioritaPolitica
          AND rel.active = true
    """)
    void softDeleteByPrioritaPoliticaId(
        @Param("idPrioritaPolitica") Long idPrioritaPolitica,
        @Param("deactivationTime") LocalDateTime deactivationTime);
}
