package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AreaOrganizzativaDTO;
import it.ey.piao.api.service.IAreaOrganizzativaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/aree-organizzative")
public class AreaOrganizzativaController {

    private final IAreaOrganizzativaService areaOrganizzativaService;

    public AreaOrganizzativaController(IAreaOrganizzativaService areaOrganizzativaService) {
        this.areaOrganizzativaService = areaOrganizzativaService;
    }

    @GetMapping("/sezione/{idSezione1}")
    public ResponseEntity<List<AreaOrganizzativaDTO>> findBySezione1(@PathVariable Long idSezione1) {
        return ResponseEntity.ok(areaOrganizzativaService.findByidSezione1(idSezione1));
    }

    @GetMapping("/piao/{piaoId}")
    public ResponseEntity<List<AreaOrganizzativaDTO>> findByPiao(@PathVariable Long piaoId) {
        return ResponseEntity.ok(areaOrganizzativaService.findByPiaoId(piaoId));
    }

    @PostMapping("/save")
    public ResponseEntity<AreaOrganizzativaDTO> save(@RequestBody AreaOrganizzativaDTO request) {
        return ResponseEntity.ok(areaOrganizzativaService.save(request));
    }
}

