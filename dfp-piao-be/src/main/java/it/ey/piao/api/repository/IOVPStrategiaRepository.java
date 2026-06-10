package it.ey.piao.api.repository;

import it.ey.entity.OVPStrategia;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IOVPStrategiaRepository extends BaseRepository<OVPStrategia, Long> {
    @Query("SELECT os FROM OVPStrategia os WHERE os.ovp.id = :ovpId")
    List<OVPStrategia> findByOvpId(@Param("ovpId") Long ovpId);



    @Modifying
    @Query("""
    UPDATE OVPStrategia os
    SET os.active = false,
        os.deactivationTime = :deactivationTime
    WHERE os.id = :id
    """)
    void softDeleteById(@Param("id") Long id,
                        @Param("deactivationTime") LocalDateTime deactivationTime);

    @Modifying
    @Query("""
    UPDATE OVPStrategia os
    SET os.active = false,
        os.deactivationTime = :deactivationTime
    WHERE os.ovp.id = :ovpId
      AND os.active = true
""")
    void softDeleteByOvpId(@Param("ovpId") Long ovpId,
                           @Param("deactivationTime") LocalDateTime deactivationTime);

}
