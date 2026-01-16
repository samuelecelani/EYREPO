package it.ey.piao.api.service;

import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;

import java.util.List;

public interface IOVPStrategiaService {

    OVPStrategiaDTO save(OVPStrategiaDTO request);
    List<OVPStrategiaDTO> save(List<OVPStrategiaDTO> request);

    OVPStrategiaIndicatoreDTO saveIndicatore(OVPStrategiaIndicatoreDTO request);
}
