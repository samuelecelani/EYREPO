package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import it.ey.piao.bff.service.IObiettivoPrevenzioneCorruzioneTrasparenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/obiettivo-prevenzione-corruzione-trasparenza")
public class ObiettivoPrevenzioneCorruzioneTrasparenzaController {


    private final IObiettivoPrevenzioneCorruzioneTrasparenzaService obiettivoPrevenzioneCorruzioneTrasparenzaService ;

    public ObiettivoPrevenzioneCorruzioneTrasparenzaController(  IObiettivoPrevenzioneCorruzioneTrasparenzaService obiettivoPrevenzioneCorruzioneTrasparenzaService) {
        this.obiettivoPrevenzioneCorruzioneTrasparenzaService = obiettivoPrevenzioneCorruzioneTrasparenzaService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>>> save(@RequestBody ObiettivoPrevenzioneCorruzioneTrasparenzaDTO request) {
        return obiettivoPrevenzioneCorruzioneTrasparenzaService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione23/{idSezione23}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>>>> getAllBySezione23(@PathVariable Long idSezione23) {
        return obiettivoPrevenzioneCorruzioneTrasparenzaService.getAllBySezione23(idSezione23)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return obiettivoPrevenzioneCorruzioneTrasparenzaService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
