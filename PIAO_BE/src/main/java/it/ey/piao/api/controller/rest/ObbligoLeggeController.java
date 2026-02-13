package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ObbligoLeggeDTO;
import it.ey.piao.api.service.IObbligoLeggeService;
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
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        obbligoLeggeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
