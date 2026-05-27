package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttivitaSensibileDTO;
import it.ey.piao.api.service.IAttivitaSensibileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/attivita-sensibile")

public class AttivitaSensibileController {
    private final IAttivitaSensibileService attivitaSensibileService;


    public AttivitaSensibileController(IAttivitaSensibileService attivitaSensibileService) {
        this.attivitaSensibileService = attivitaSensibileService;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveOrUpdate(@RequestBody AttivitaSensibileDTO request) {
        attivitaSensibileService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }


    /**
     * Recupera tutte le Attivita Sensibili  per una Sezione23.
     */
    @GetMapping("/sezione23/{idSezione23}")
    public ResponseEntity<List<AttivitaSensibileDTO>> getAllBySezione23(@PathVariable Long idSezione23) {
        List<AttivitaSensibileDTO> response = attivitaSensibileService.getAllBySezione23(idSezione23);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un attivita sensibile per ID.
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
        attivitaSensibileService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }


}
