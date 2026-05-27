package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AvvisoDTO;
import it.ey.piao.api.service.IAvvisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/avvisi")
public class AvvisoController {

    private final IAvvisoService avvisoService;

    public AvvisoController(IAvvisoService avvisoService) {
        this.avvisoService = avvisoService;
    }

    @GetMapping
    public ResponseEntity<List<AvvisoDTO>> getAll() {
        //check su avvisi senza tipologia (NON MOSTRARE)
        //delete avvisi che hanno più di 24H di distacco tra la sua creazione e la data odierna
        List<AvvisoDTO> response = avvisoService.getAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvvisoDTO> getById(@PathVariable Long id) {
        AvvisoDTO response = avvisoService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AvvisoDTO> create(@RequestBody AvvisoDTO avvisoDTO) {
        AvvisoDTO response = avvisoService.create(avvisoDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvvisoDTO> update(@PathVariable Long id, @RequestBody AvvisoDTO avvisoDTO) {
        AvvisoDTO response = avvisoService.update(id, avvisoDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        avvisoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

