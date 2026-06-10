package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.IndicatoreDTO;
import it.ey.piao.api.service.IIndicatoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/indicatore")
public class IndicatoreController {

    private final IIndicatoreService iIndicatoreService;


    public IndicatoreController(IIndicatoreService iIndicatoreService) {
        this.iIndicatoreService = iIndicatoreService;
    }

    @PostMapping("/save")
    public ResponseEntity<IndicatoreDTO> save(@RequestBody IndicatoreDTO request) {
        return ResponseEntity.ok(iIndicatoreService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<IndicatoreDTO>> findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(@RequestParam Long idPiao,
                                                       @RequestParam Long idEntitaFK,
                                                       @RequestParam String codTipologiaFK) {
        return ResponseEntity.ok(iIndicatoreService.findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(idPiao, idEntitaFK, codTipologiaFK));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           HttpServletRequest request) {

        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");

        // Delete logica puntuale (soft delete + gestione tabella associativa nel service)
        iIndicatoreService.deleteById(
            id,
            campiModificati,
            idPiao,
            testoSezione,
            updatedByNameSurname,
            updatedByRole,
            statoSezione
        );

        return ResponseEntity.noContent().build();
    }

}
