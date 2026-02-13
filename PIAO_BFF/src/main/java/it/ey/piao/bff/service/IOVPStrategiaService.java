package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IOVPStrategiaService {
    public Mono<GenericResponseDTO<OVPStrategiaDTO>> save(OVPStrategiaDTO request, Long idOVP);
    public Mono<GenericResponseDTO<Void>> delete(Long id);

}
