package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.piao.api.service.IEventoRischioService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        eventoRischioService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina tutti gli EventiRischio associati a un'AttivitaSensibile.
     */
    @DeleteMapping("/attivita-sensibile/{idAttivitaSensibile}")
    public ResponseEntity<Void> deleteByAttivitaSensibile(@PathVariable Long idAttivitaSensibile,
                                                          @RequestParam(required = false) String campiModificati,
                                                          @RequestParam(required = false) Long idPiao,
                                                          @RequestParam(required = false) String testoSezione,
                                                          HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        eventoRischioService.deleteByAttivitaSensibile(idAttivitaSensibile, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
