package it.ey.piao.api.repository;

import it.ey.entity.OVPAreaOrganizzativa;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IOVPAreaOrganizzativaRepository extends BaseRepository<OVPAreaOrganizzativa,Long> {


      //Soft delete relazioni quando viene eliminato un OVP (lato OVP)
    @Modifying
    @Query("""
        UPDATE OVPAreaOrganizzativa rel
        SET rel.active = false,
            rel.deactivationTime = :deactivationTime
        WHERE rel.ovp.id = :idOvp
          AND rel.active = true
    """)
    void softDeleteByOvpId(@Param("idOvp") Long idOvp,
                           @Param("deactivationTime") LocalDateTime deactivationTime);

     // Soft delete relazioni quando viene eliminata un'Area Organizzativa (lato AreaOrganizzativa)
    @Modifying
    @Query("""
        UPDATE OVPAreaOrganizzativa rel
        SET rel.active = false,
            rel.deactivationTime = :deactivationTime
        WHERE rel.areaOrganizzativa.id = :idAreaOrganizzativa
          AND rel.active = true
    """)
    void softDeleteByAreaOrganizzativaId(@Param("idAreaOrganizzativa") Long idAreaOrganizzativa,
                                         @Param("deactivationTime") LocalDateTime deactivationTime);

}
