package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AvvisoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAvvisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/avvisi")
public class AvvisoController {

    private final IAvvisoService avvisoService;

    public AvvisoController(IAvvisoService avvisoService) {
        this.avvisoService = avvisoService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<AvvisoDTO>>>> getAvvisi(@RequestParam String modulo) {
        return avvisoService.getAvvisi(modulo)
            .map(ResponseEntity::ok);
    }


}
