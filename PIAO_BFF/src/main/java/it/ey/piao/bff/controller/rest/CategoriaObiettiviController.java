package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.ICategoriaObiettiviService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/categoria-obiettivi")
public class CategoriaObiettiviController {

    private final ICategoriaObiettiviService categoriaObiettiviService;

    public CategoriaObiettiviController(ICategoriaObiettiviService categoriaObiettiviService) {
        this.categoriaObiettiviService = categoriaObiettiviService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<CategoriaObiettiviDTO>>> save(@RequestBody CategoriaObiettiviDTO request) {
        return categoriaObiettiviService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione4/{idSezione4}")
    public Mono<ResponseEntity<GenericResponseDTO<List<CategoriaObiettiviDTO>>>> getAllBySezione4(@PathVariable Long idSezione4) {
        return categoriaObiettiviService.getAllBySezione4(idSezione4)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return categoriaObiettiviService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
