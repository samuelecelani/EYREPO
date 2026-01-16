package it.ey.piao.api.repository;

import it.ey.entity.OVPStrategiaIndicatore;
import it.ey.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IOVPStrategiaIndicatoreRepository extends BaseRepository<OVPStrategiaIndicatore, Long> {

    //recupero delle OVPStrategiaIndicatores tramite una lista di idOVP
    @Query(" SELECT oSI FROM OVPStrategiaIndicatore oSI JOIN oSI.ovpStrategia s WHERE s.id IN :idOVP")
    List<OVPStrategiaIndicatore> getOVPStrategiaIndicatoresByIdOVP(@Param("idOVP") List<Long> idOVP);

}
