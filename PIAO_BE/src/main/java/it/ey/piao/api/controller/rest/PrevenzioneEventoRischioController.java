package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.piao.api.service.IMisuraPrevenzioneEventoRischioService;
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
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        prevenzioneEventoRischioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }


}
