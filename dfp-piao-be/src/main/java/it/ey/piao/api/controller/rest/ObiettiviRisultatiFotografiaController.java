package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.*;
import it.ey.piao.api.service.IObiettiviRisultatiFotografiaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/obiettivi-risultati-fotografia")
public class ObiettiviRisultatiFotografiaController {

    private final IObiettiviRisultatiFotografiaService obiettiviRisultatiFotografiaService;

    public ObiettiviRisultatiFotografiaController(IObiettiviRisultatiFotografiaService obiettiviRisultatiFotografiaService) {
        this.obiettiviRisultatiFotografiaService = obiettiviRisultatiFotografiaService;
    }

    @PostMapping("/save")
    public ResponseEntity<ObiettiviRisultatiFotografiaDTO> saveOrUpdate(@RequestBody ObiettiviRisultatiFotografiaDTO request) {
        ObiettiviRisultatiFotografiaDTO response = obiettiviRisultatiFotografiaService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idObiettivoFotografia}")
    public ResponseEntity<Void> delete(@PathVariable Long idObiettivoFotografia,
                                       @RequestParam(required = false) String campiModificati,
                                       @RequestParam(required = false) Long idPiao,
                                       @RequestParam(required = false) String testoSezione,
                                       HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        obiettiviRisultatiFotografiaService.deleteById(idObiettivoFotografia, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/obiettivi-risultati/{idSezione332}")
    public ResponseEntity<List<ObiettiviRisultatiFotografiaDTO>> getObiettiviBySezione(@PathVariable Long idSezione332) {
        List<ObiettiviRisultatiFotografiaDTO> response = obiettiviRisultatiFotografiaService.getObiettiviRisultatiByIdSezione332(idSezione332);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fotografia-formazione/{idSezione332}")
    public ResponseEntity<List<ObiettiviRisultatiFotografiaDTO>> getFotografiaBySezione(@PathVariable Long idSezione332) {
        List<ObiettiviRisultatiFotografiaDTO> response = obiettiviRisultatiFotografiaService.getFotografieFormazioneByIdSezione332(idSezione332);
        return ResponseEntity.ok(response);
    }
}
