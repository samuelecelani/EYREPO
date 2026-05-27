package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.piao.api.service.IMisuraPrevenzioneEventoRischioService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/prevenzione-evento-rischio")
public class PrevenzioneEventoRischioController {
    private final IMisuraPrevenzioneEventoRischioService prevenzioneEventoRischioService;


    public PrevenzioneEventoRischioController(IMisuraPrevenzioneEventoRischioService prevenzioneEventoRischioService) {
        this.prevenzioneEventoRischioService = prevenzioneEventoRischioService;

    }

    @PostMapping("/save")
    public ResponseEntity<MisuraPrevenzioneEventoRischioDTO> saveOrUpdate(@RequestBody MisuraPrevenzioneEventoRischioDTO request) {
   prevenzioneEventoRischioService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }


    /**
     * Recupera tutti gli obiettivi di performance per una Sezione23.
     */
    @GetMapping("/evento-rischio/{idEventoRischio}")
    public ResponseEntity<List<MisuraPrevenzioneEventoRischioDTO>> getAllByEventoRischio(@PathVariable Long idEventoRischio) {
        List<MisuraPrevenzioneEventoRischioDTO> response = prevenzioneEventoRischioService.getAllByEventoRischio(idEventoRischio);
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
                                           HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        prevenzioneEventoRischioService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }


}
