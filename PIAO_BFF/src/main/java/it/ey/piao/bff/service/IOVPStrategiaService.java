package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IOVPStrategiaService {
    Mono<GenericResponseDTO<OVPStrategiaDTO>> save(OVPStrategiaDTO request, Long idOVP);
    Mono<GenericResponseDTO<List<OVPStrategiaDTO>>> findByOvpId(Long idOvp);
    Mono<GenericResponseDTO<Void>> delete(Long id);
}
