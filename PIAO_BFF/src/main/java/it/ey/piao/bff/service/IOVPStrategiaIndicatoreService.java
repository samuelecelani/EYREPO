package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import reactor.core.publisher.Mono;

public interface IOVPStrategiaIndicatoreService {
    Mono<GenericResponseDTO<OVPStrategiaIndicatoreDTO>> saveIndicatore(OVPStrategiaIndicatoreDTO request, Long idStrategia);
    Mono<GenericResponseDTO<Void>> deleteById(Long id);
}
