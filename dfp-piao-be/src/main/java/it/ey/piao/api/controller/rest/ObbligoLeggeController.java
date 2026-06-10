package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ObbligoLeggeDTO;
import it.ey.piao.api.service.IObbligoLeggeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/obbligo-legge")
public class ObbligoLeggeController {
    private final IObbligoLeggeService obbligoLeggeService;

    public ObbligoLeggeController(IObbligoLeggeService obbligoLeggeService) {
        this.obbligoLeggeService = obbligoLeggeService;
    }

    @PostMapping("/save")
    public ResponseEntity<ObbligoLeggeDTO> saveOrUpdate(@RequestBody ObbligoLeggeDTO request) {
        ObbligoLeggeDTO response = obbligoLeggeService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }


    /**
     * Recupera tutti gli obbligo Legge per una Sezione23.
     */
    @GetMapping("/sezione23/{idSezione23}")
    public ResponseEntity<List<ObbligoLeggeDTO>> getAllBySezione23(@PathVariable Long idSezione23) {
        List<ObbligoLeggeDTO> response = obbligoLeggeService.getAllBySezione23(idSezione23);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un obbligo Legge per ID.
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
        obbligoLeggeService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
