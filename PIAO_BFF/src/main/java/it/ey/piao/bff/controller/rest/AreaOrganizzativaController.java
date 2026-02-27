package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAreaOrganizzativaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/aree-organizzative")
public class AreaOrganizzativaController {

    private final IAreaOrganizzativaService areaOrganizzativaService;

    public AreaOrganizzativaController(IAreaOrganizzativaService areaOrganizzativaService) {
        this.areaOrganizzativaService = areaOrganizzativaService;
    }

    @GetMapping("/sezione/{idSezione1}")
    public Mono<ResponseEntity<GenericResponseDTO<List<AreaOrganizzativaDTO>>>> findBySezione1(@PathVariable Long idSezione1) {
        return areaOrganizzativaService.findByidSezione1(idSezione1)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<AreaOrganizzativaDTO>>> save(@RequestBody AreaOrganizzativaDTO request) {
        return areaOrganizzativaService.save(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/piao/{piaoId}")
    public Mono<ResponseEntity<GenericResponseDTO<List<AreaOrganizzativaDTO>>>> findByPiao(@PathVariable Long piaoId) {
        return areaOrganizzativaService.findByPiaoId(piaoId)
            .map(ResponseEntity::ok);
    }
}
