package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IEventoRischioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/evento-rischio")
public class EventoRischioController {

    private final IEventoRischioService eventoRischioService;

    public EventoRischioController(IEventoRischioService eventoRischioService) {
        this.eventoRischioService = eventoRischioService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<EventoRischioDTO>>> save(@RequestBody EventoRischioDTO request) {
        return eventoRischioService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/attivita-sensibile/{idAttivitaSensibile}")
    public Mono<ResponseEntity<GenericResponseDTO<List<EventoRischioDTO>>>> getAllByAttivitaSensibile(@PathVariable Long idAttivitaSensibile) {
        return eventoRischioService.getAllByAttivitaSensibile(idAttivitaSensibile)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return eventoRischioService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @DeleteMapping("/attivita-sensibile/{idAttivitaSensibile}")
    public Mono<ResponseEntity<Void>> deleteByAttivitaSensibile(@PathVariable Long idAttivitaSensibile) {
        return eventoRischioService.deleteByAttivitaSensibile(idAttivitaSensibile)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
