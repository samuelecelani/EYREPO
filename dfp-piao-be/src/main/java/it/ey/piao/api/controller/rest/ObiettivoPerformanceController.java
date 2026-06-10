package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.piao.api.service.IObbiettivoPerformanceService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           @RequestParam(defaultValue = "false") boolean forceDelete,

                                           HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        obbiettivoPerformanceService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole,forceDelete, statoSezione);
        return ResponseEntity.noContent().build();
    }


}
