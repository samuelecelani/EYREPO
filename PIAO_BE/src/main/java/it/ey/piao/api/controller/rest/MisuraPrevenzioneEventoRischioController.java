package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import it.ey.piao.api.service.IMisuraPrevenzioneEventoRischioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/misura-prevenzione-evento-rischio")
public class MisuraPrevenzioneEventoRischioController {
    private final IMisuraPrevenzioneEventoRischioService misuraPrevenzioneEventoRischioService;

    public MisuraPrevenzioneEventoRischioController(IMisuraPrevenzioneEventoRischioService misuraPrevenzioneEventoRischioService) {
        this.misuraPrevenzioneEventoRischioService = misuraPrevenzioneEventoRischioService;
    }


    @PostMapping("/save")
    public ResponseEntity<MisuraPrevenzioneEventoRischioDTO> saveOrUpdate(@RequestBody MisuraPrevenzioneEventoRischioDTO request) {
        MisuraPrevenzioneEventoRischioDTO response = misuraPrevenzioneEventoRischioService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }


    /**
     * Recupera tutte le misure Prevenzione Evento rischio per un eventoRischio
     */
    @GetMapping("/eventoRischio/{idEvento}")
    public ResponseEntity<List<MisuraPrevenzioneEventoRischioDTO>> getAllByEvento(@PathVariable Long idEvento) {
        List<MisuraPrevenzioneEventoRischioDTO> response = misuraPrevenzioneEventoRischioService.getAllByEventoRischio(idEvento);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una misura di prevenzione per ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        misuraPrevenzioneEventoRischioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
