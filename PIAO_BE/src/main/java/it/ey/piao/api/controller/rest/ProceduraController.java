package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ProceduraDTO;
import it.ey.piao.api.service.IProceduraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/procedura")
public class ProceduraController {

    private final IProceduraService proceduraService;

    public ProceduraController(IProceduraService proceduraService) {
        this.proceduraService = proceduraService;
    }

    @PostMapping("/save")
    public ResponseEntity<ProceduraDTO> save(@RequestBody ProceduraDTO request) {
        return ResponseEntity.ok(proceduraService.save(request));
    }

    @GetMapping("/sezione/{idSezione21}")
    public ResponseEntity<List<ProceduraDTO>> getProcedureBySezione(@PathVariable Long idSezione21) {
        return ResponseEntity.ok(proceduraService.getProcedure(idSezione21));
    }
}

