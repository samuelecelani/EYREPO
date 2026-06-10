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
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione) {
        return eventoRischioService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @DeleteMapping("/attivita-sensibile/{idAttivitaSensibile}")
    public Mono<ResponseEntity<Void>> deleteByAttivitaSensibile(@PathVariable Long idAttivitaSensibile,
                                                                @RequestParam(required = false) String campiModificati,
                                                                @RequestParam(required = false) Long idPiao,
                                                                @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione) {
        return eventoRischioService.deleteByAttivitaSensibile(idAttivitaSensibile, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
