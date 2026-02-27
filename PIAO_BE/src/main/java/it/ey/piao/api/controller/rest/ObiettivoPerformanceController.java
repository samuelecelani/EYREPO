package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.piao.api.service.IObbiettivoPerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/obiettivo-performance")
public class ObiettivoPerformanceController {

    private final IObbiettivoPerformanceService obbiettivoPerformanceService;

    public ObiettivoPerformanceController(IObbiettivoPerformanceService obbiettivoPerformanceService) {
        this.obbiettivoPerformanceService = obbiettivoPerformanceService;
    }

    /**
     * Crea o aggiorna un obiettivo di performance.
     * Se l'ID è presente nel DTO, viene effettuato un update.
     * Se l'ID è null, viene creato un nuovo obiettivo.
     */
    @PostMapping("/save")
    public ResponseEntity<ObbiettivoPerformanceDTO> saveOrUpdate(@RequestBody ObbiettivoPerformanceDTO request) {
        ObbiettivoPerformanceDTO response = obbiettivoPerformanceService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }


    /**
     * Recupera tutti gli obiettivi di performance per una Sezione22.
     */
    @GetMapping("/sezione22/{idSezione22}")
    public ResponseEntity<List<ObbiettivoPerformanceDTO>> getAllBySezione22(@PathVariable Long idSezione22) {
        List<ObbiettivoPerformanceDTO> response = obbiettivoPerformanceService.getAllBySezione22(idSezione22);
        return ResponseEntity.ok(response);
    }

    /**
     * Recupera gli obiettivi di performance filtrati per tipologia (obbligatoria),
     * idOvp e idStrategia (opzionali).
     * Se idOvp o idStrategia sono null, verranno cercati record con il campo corrispondente null sul DB.
     */
    @GetMapping("/filter")
    public ResponseEntity<List<ObbiettivoPerformanceDTO>> findByTipologiaAndFilters(
            @RequestParam TipologiaObbiettivo tipologia,
            @RequestParam(required = false) Long idOvp,
            @RequestParam(required = false) Long idStrategia) {
        List<ObbiettivoPerformanceDTO> response = obbiettivoPerformanceService.findByTipologiaAndFilters(tipologia, idOvp, idStrategia);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un obiettivo di performance per ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        obbiettivoPerformanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
