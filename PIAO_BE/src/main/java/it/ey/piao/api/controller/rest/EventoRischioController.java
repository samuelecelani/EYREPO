package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.piao.api.service.IEventoRischioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/evento-rischio")
public class EventoRischioController {

    private final IEventoRischioService eventoRischioService;

    public EventoRischioController(IEventoRischioService eventoRischioService) {
        this.eventoRischioService = eventoRischioService;
    }

    @PostMapping("/save")
    public ResponseEntity<EventoRischioDTO> saveOrUpdate(@RequestBody EventoRischioDTO request) {
        EventoRischioDTO response = eventoRischioService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Recupera tutti gli EventiRischio per un'AttivitaSensibile.
     */
    @GetMapping("/attivita-sensibile/{idAttivitaSensibile}")
    public ResponseEntity<List<EventoRischioDTO>> getAllByAttivitaSensibile(@PathVariable Long idAttivitaSensibile) {
        List<EventoRischioDTO> response = eventoRischioService.getAllByAttivitaSensibile(idAttivitaSensibile);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un EventoRischio per ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        eventoRischioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina tutti gli EventiRischio associati a un'AttivitaSensibile.
     */
    @DeleteMapping("/attivita-sensibile/{idAttivitaSensibile}")
    public ResponseEntity<Void> deleteByAttivitaSensibile(@PathVariable Long idAttivitaSensibile) {
        eventoRischioService.deleteByAttivitaSensibile(idAttivitaSensibile);
        return ResponseEntity.noContent().build();
    }
}
