package it.ey.piao.bff.controller.rest;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPStrategiaDTO;
import it.ey.dto.OVPStrategiaIndicatoreDTO;
import it.ey.piao.bff.service.IOVPStrategiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/ovp-strategia")
public class OVPStrategiaController {

    private final IOVPStrategiaService iovpStrategiaService;

    public OVPStrategiaController(IOVPStrategiaService iovpStrategiaService) {
        this.iovpStrategiaService = iovpStrategiaService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<OVPStrategiaDTO>>> save(@RequestBody OVPStrategiaDTO request) {
        return iovpStrategiaService.save(request)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/saveAll")
    public Mono<ResponseEntity<GenericResponseDTO<List<OVPStrategiaDTO>>>> saveAll(@RequestBody List<OVPStrategiaDTO> request) {
        return iovpStrategiaService.save(request).map(ResponseEntity::ok);
    }

    @PostMapping("/saveIndicatore")
    public Mono<ResponseEntity<GenericResponseDTO<OVPStrategiaIndicatoreDTO>>> saveIndicatore(@RequestBody OVPStrategiaIndicatoreDTO request) {
        return iovpStrategiaService.saveIndicatore(request).map(ResponseEntity::ok);
    }
}
