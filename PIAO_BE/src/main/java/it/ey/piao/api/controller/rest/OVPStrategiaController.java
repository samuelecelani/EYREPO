package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.OVPStrategiaDTO;
import it.ey.piao.api.service.IOVPStrategiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/ovp-strategia")
public class OVPStrategiaController {

    private final IOVPStrategiaService iovpStrategiaService;

    public OVPStrategiaController(IOVPStrategiaService iovpStrategiaService) {
        this.iovpStrategiaService = iovpStrategiaService;
    }

    @PostMapping("/save/{idOVP}")
    public ResponseEntity<OVPStrategiaDTO> save(@RequestBody OVPStrategiaDTO request, @PathVariable Long idOVP) {
        return ResponseEntity.ok(iovpStrategiaService.save(request, idOVP));
    }

    @GetMapping("/ovp/{idOvp}")
    public ResponseEntity<List<OVPStrategiaDTO>> findByOvpId(@PathVariable Long idOvp) {
        return ResponseEntity.ok(iovpStrategiaService.findByOvpId(idOvp));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        iovpStrategiaService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
