package it.ey.piao.api.controller.rest;

import it.ey.dto.PrioritaPoliticaDTO;
import it.ey.piao.api.service.IPrioritaPoliticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/priorita-politiche")
public class PrioritaPoliticaController {

    private final IPrioritaPoliticaService prioritaPoliticaService;

    public PrioritaPoliticaController(IPrioritaPoliticaService prioritaPoliticaService) {
        this.prioritaPoliticaService = prioritaPoliticaService;
    }

    @GetMapping("/sezione/{idSezione1}")
    public ResponseEntity<List<PrioritaPoliticaDTO>> findBySezione1(@PathVariable Long idSezione1) {
        return ResponseEntity.ok(prioritaPoliticaService.findByidSezione1(idSezione1));
    }

    @PostMapping("/save")
    public ResponseEntity<PrioritaPoliticaDTO> save(@RequestBody PrioritaPoliticaDTO request) {
        return ResponseEntity.ok(prioritaPoliticaService.save(request));
    }
}

