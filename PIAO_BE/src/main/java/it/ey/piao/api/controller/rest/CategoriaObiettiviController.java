package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.piao.api.service.ICategoriaObiettiviService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/categoria-obiettivi")
public class CategoriaObiettiviController {

    private final ICategoriaObiettiviService categoriaObiettiviService;

    public CategoriaObiettiviController(ICategoriaObiettiviService categoriaObiettiviService) {
        this.categoriaObiettiviService = categoriaObiettiviService;
    }

    @PostMapping("/save")
    public ResponseEntity<CategoriaObiettiviDTO> saveOrUpdate(@RequestBody CategoriaObiettiviDTO dto) {
        CategoriaObiettiviDTO saved = categoriaObiettiviService.saveOrUpdate(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/sezione4/{idSezione4}")
    public ResponseEntity<List<CategoriaObiettiviDTO>> getAllBySezione4(@PathVariable Long idSezione4) {
        List<CategoriaObiettiviDTO> result = categoriaObiettiviService.getAllBySezione4(idSezione4);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        categoriaObiettiviService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
