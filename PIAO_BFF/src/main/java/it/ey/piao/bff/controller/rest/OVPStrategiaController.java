package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaDTO;
import it.ey.piao.bff.service.IOVPStrategiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/ovp-strategia")
public class OVPStrategiaController {

    private final IOVPStrategiaService iovpStrategiaService;

    public OVPStrategiaController(IOVPStrategiaService iovpStrategiaService) {
        this.iovpStrategiaService = iovpStrategiaService;
    }

    @PostMapping("/save/{idOVP}")
    public Mono<ResponseEntity<GenericResponseDTO<OVPStrategiaDTO>>> save(@RequestBody OVPStrategiaDTO request, @PathVariable Long idOVP) {
        return iovpStrategiaService.save(request, idOVP)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/ovp/{idOvp}")
    public Mono<ResponseEntity<GenericResponseDTO<List<OVPStrategiaDTO>>>> findByOvpId(@PathVariable Long idOvp) {
        return iovpStrategiaService.findByOvpId(idOvp)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(@PathVariable Long id) {
        return iovpStrategiaService.delete(id)
            .map(ResponseEntity::ok);
    }
}
