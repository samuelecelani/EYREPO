package it.ey.piao.api.repository;

import it.ey.entity.OVPStrategia;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IOVPStrategiaRepository extends BaseRepository<OVPStrategia, Long> {
    @Query("SELECT os FROM OVPStrategia os WHERE os.ovp.id = :ovpId")
    List<OVPStrategia> findByOvpId(@Param("ovpId") Long ovpId);
}
