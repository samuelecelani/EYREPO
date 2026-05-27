package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AvvisoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAvvisoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/avvisi")
public class AvvisoController {

    private final IAvvisoService avvisoService;

    public AvvisoController(IAvvisoService avvisoService) {
        this.avvisoService = avvisoService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<AvvisoDTO>>>> getAll() {
        return avvisoService.getAll()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<AvvisoDTO>>> getById(@PathVariable Long id) {
        return avvisoService.getById(id)
            .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<GenericResponseDTO<AvvisoDTO>>> create(@RequestBody AvvisoDTO avvisoDTO) {
        return avvisoService.create(avvisoDTO)
            .map(ResponseEntity::ok);
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<AvvisoDTO>>> update(@PathVariable Long id, @RequestBody AvvisoDTO avvisoDTO) {
        return avvisoService.update(id, avvisoDTO)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(@PathVariable Long id) {
        return avvisoService.delete(id)
            .map(ResponseEntity::ok);
    }
}
