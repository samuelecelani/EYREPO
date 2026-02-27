package it.ey.piao.api.repository;

import it.ey.entity.OVPStrategia;
import it.ey.entity.OVPStrategiaIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IOVPStrategiaIndicatoreRepository extends BaseRepository<OVPStrategiaIndicatore, Long> {
    // Trova tutti gli indicatori per una specifica strategia
    List<OVPStrategiaIndicatore> findByOvpStrategia(OVPStrategia ovpStrategia);

    // Elimina tutti gli indicatori per una specifica strategia
    void deleteByOvpStrategia(OVPStrategia ovpStrategia);

}
