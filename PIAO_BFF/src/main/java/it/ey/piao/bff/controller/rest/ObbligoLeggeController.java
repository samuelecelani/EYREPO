package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObbligoLeggeDTO;
import it.ey.piao.bff.service.IObbligoLeggeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/obbligo-legge")
public class ObbligoLeggeController {
    private final IObbligoLeggeService obbligoLeggeService;


    public ObbligoLeggeController(IObbligoLeggeService obbligoLeggeService) {
        this.obbligoLeggeService = obbligoLeggeService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<ObbligoLeggeDTO>>> save(@RequestBody ObbligoLeggeDTO request) {
        return obbligoLeggeService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione23/{idSezione23}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObbligoLeggeDTO>>>> getAllBySezione23(@PathVariable Long idSezione23) {
        return obbligoLeggeService.getAllBySezione23(idSezione23)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return obbligoLeggeService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
