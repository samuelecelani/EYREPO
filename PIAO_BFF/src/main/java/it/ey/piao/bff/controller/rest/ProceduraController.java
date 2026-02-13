package it.ey.piao.bff.controller.rest;


import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ProceduraDTO;
import it.ey.piao.bff.service.IProceduraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/procedure")
public class ProceduraController {

    private final IProceduraService proceduraService;

    public ProceduraController(IProceduraService proceduraService) {
        this.proceduraService = proceduraService;
    }

    @GetMapping("/sezione1/{idSezione1}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ProceduraDTO>>>> findBySezione1(@PathVariable Long idSezione1) {
        return proceduraService.getProcedure(idSezione1)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<ProceduraDTO>>> save(@RequestBody ProceduraDTO request) {
        return proceduraService.save(request)
            .map(ResponseEntity::ok);
    }
}

