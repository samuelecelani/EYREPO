package it.ey.piao.api.controller.rest;

import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import it.ey.piao.api.service.IOVPStrategiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ovp-strategia")
public class OVPStrategiaController {

    private final IOVPStrategiaService iovpStrategiaService;

    public OVPStrategiaController(IOVPStrategiaService iovpStrategiaService) {
        this.iovpStrategiaService = iovpStrategiaService;
    }

    @PostMapping("/save")
    public ResponseEntity<OVPStrategiaDTO> save(@RequestBody OVPStrategiaDTO request) {
        return ResponseEntity.ok(iovpStrategiaService.save(request));
    }

    @PostMapping("/saveAll")
    public ResponseEntity<OVPStrategiaDTO> saveAll(@RequestBody OVPStrategiaDTO request) {
        return ResponseEntity.ok(iovpStrategiaService.save(request));
    }

    @PostMapping("/save-indicatore")
    public ResponseEntity<OVPStrategiaIndicatoreDTO> saveIndicatore(@RequestBody OVPStrategiaIndicatoreDTO request) {
        return ResponseEntity.ok(iovpStrategiaService.saveIndicatore(request));
    }
}
