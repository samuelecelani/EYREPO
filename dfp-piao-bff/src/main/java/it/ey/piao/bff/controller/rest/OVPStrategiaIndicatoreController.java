package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import it.ey.piao.bff.service.IOVPStrategiaIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/ovp-strategia-indicatore")
public class OVPStrategiaIndicatoreController {

    private final IOVPStrategiaIndicatoreService iovpStrategiaIndicatoreService;

    public OVPStrategiaIndicatoreController(IOVPStrategiaIndicatoreService iovpStrategiaIndicatoreService) {
        this.iovpStrategiaIndicatoreService = iovpStrategiaIndicatoreService;
    }

    @PostMapping("/save/{idStrategia}")
    public Mono<ResponseEntity<GenericResponseDTO<OVPStrategiaIndicatoreDTO>>> saveIndicatoreStrategia(@RequestBody OVPStrategiaIndicatoreDTO request, @PathVariable Long idStrategia) {
        return iovpStrategiaIndicatoreService.saveIndicatore(request, idStrategia)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(@PathVariable Long id) {
        return iovpStrategiaIndicatoreService.deleteById(id)
            .map(ResponseEntity::ok);
    }
}
