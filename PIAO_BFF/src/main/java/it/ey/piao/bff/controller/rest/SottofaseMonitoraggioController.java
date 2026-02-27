package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.SottofaseMonitoraggioDTO;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.piao.bff.service.ISottofaseMonitoraggioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/sottofase-monitoraggio")
public class SottofaseMonitoraggioController {

    private final ISottofaseMonitoraggioService sottofaseMonitoraggioService;

    public SottofaseMonitoraggioController(ISottofaseMonitoraggioService sottofaseMonitoraggioService) {
        this.sottofaseMonitoraggioService = sottofaseMonitoraggioService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<SottofaseMonitoraggioDTO>>> save(@RequestBody SottofaseMonitoraggioDTO request) {
        return sottofaseMonitoraggioService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione4/{idSezione4}")
    public Mono<ResponseEntity<GenericResponseDTO<List<SottofaseMonitoraggioDTO>>>> getAllBySezione4(@PathVariable Long idSezione4) {
        return sottofaseMonitoraggioService.getAllBySezione4(idSezione4)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return sottofaseMonitoraggioService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
