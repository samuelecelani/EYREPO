package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PrioritaPoliticaDTO;
import it.ey.piao.api.service.IPrioritaPoliticaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/priorita-politiche")
public class PrioritaPoliticaController {

    private final IPrioritaPoliticaService prioritaPoliticaService;

    public PrioritaPoliticaController(IPrioritaPoliticaService prioritaPoliticaService) {
        this.prioritaPoliticaService = prioritaPoliticaService;
    }

    @GetMapping("/sezione/{idSezione1}")
    public ResponseEntity<List<PrioritaPoliticaDTO>> findBySezione1(@PathVariable Long idSezione1) {
        return ResponseEntity.ok(prioritaPoliticaService.findByidSezione1(idSezione1));
    }

    @GetMapping("/piao/{piaoId}")
    public ResponseEntity<List<PrioritaPoliticaDTO>> findByPiao(@PathVariable Long piaoId) {
        return ResponseEntity.ok(prioritaPoliticaService.findByPiaoId(piaoId));
    }

    @PostMapping("/save")
    public ResponseEntity<PrioritaPoliticaDTO> save(@RequestBody PrioritaPoliticaDTO request) {
        return ResponseEntity.ok(prioritaPoliticaService.save(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           @RequestParam(defaultValue = "false") boolean forceDelete,
                                           HttpServletRequest request) {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        prioritaPoliticaService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, forceDelete, statoSezione);
        return ResponseEntity.noContent().build();
    }
}

