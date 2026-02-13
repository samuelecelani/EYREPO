package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttivitaSensibileDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAttivitaSensibileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/attivita-sensibile")
public class AttivitaSensibileController {
    private final IAttivitaSensibileService attivitaSensibileService;

    public AttivitaSensibileController(IAttivitaSensibileService attivitaSensibileService) {
        this.attivitaSensibileService = attivitaSensibileService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<AttivitaSensibileDTO>>> save(@RequestBody AttivitaSensibileDTO request) {
        return attivitaSensibileService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione23/{idSezione23}")
    public Mono<ResponseEntity<GenericResponseDTO<List<AttivitaSensibileDTO>>>> getAllBySezione22(@PathVariable Long idSezione23) {
        return attivitaSensibileService.getAllBySezione23(idSezione23)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return attivitaSensibileService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }


}
