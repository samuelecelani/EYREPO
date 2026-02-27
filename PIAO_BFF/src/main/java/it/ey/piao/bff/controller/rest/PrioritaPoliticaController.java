package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PrioritaPoliticaDTO;
import it.ey.piao.bff.service.IPrioritaPoliticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/priorita-politiche")
public class PrioritaPoliticaController {

    private final IPrioritaPoliticaService prioritaPoliticaService;

    public PrioritaPoliticaController(IPrioritaPoliticaService prioritaPoliticaService) {
        this.prioritaPoliticaService = prioritaPoliticaService;
    }

    @GetMapping("/sezione1/{idSezione1}")
    public Mono<ResponseEntity<GenericResponseDTO<List<PrioritaPoliticaDTO>>>> findBySezione1(@PathVariable Long idSezione1) {
        return prioritaPoliticaService.findByidSezione1(idSezione1)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<PrioritaPoliticaDTO>>> save(@RequestBody PrioritaPoliticaDTO request) {
        return prioritaPoliticaService.save(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/piao/{piaoId}")
    public Mono<ResponseEntity<GenericResponseDTO<List<PrioritaPoliticaDTO>>>> findByPiao(@PathVariable Long piaoId) {
        return prioritaPoliticaService.findByPiaoId(piaoId)
            .map(ResponseEntity::ok);
    }
}
