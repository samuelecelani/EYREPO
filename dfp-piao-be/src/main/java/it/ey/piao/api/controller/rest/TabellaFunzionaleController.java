package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.TabellaFunzionaleDTO;
import it.ey.piao.api.service.ITabellaFunzionaleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/tabella-funzionale")
public class TabellaFunzionaleController {

    private final ITabellaFunzionaleService tabellaFunzionaleService;

    public TabellaFunzionaleController(ITabellaFunzionaleService tabellaFunzionaleService) {
        this.tabellaFunzionaleService = tabellaFunzionaleService;
    }

    @GetMapping("/sezione/{codTipologiaFK}/{idEntitaFK}")
    public ResponseEntity<List<TabellaFunzionaleDTO>> findByEntitaAndTipologia(
            @PathVariable String codTipologiaFK,
            @PathVariable Long idEntitaFK) {
        return ResponseEntity.ok(tabellaFunzionaleService.findByIdEntitaFKAndCodTipologiaFK(idEntitaFK, codTipologiaFK));
    }

    @PostMapping("/save")
    public ResponseEntity<TabellaFunzionaleDTO> save(@RequestBody TabellaFunzionaleDTO request) {
        return ResponseEntity.ok(tabellaFunzionaleService.save(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String codTipologiaFK,
                                           @RequestParam(required = false) String testoSezione,
                                           HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        tabellaFunzionaleService.deleteById(id, campiModificati, idPiao, codTipologiaFK, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
