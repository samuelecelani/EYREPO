package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import it.ey.piao.api.service.IObiettivoPrevenzioneCorruzioneTrasparenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/obiettivo-prevenzione-corruzione-trasparenza")
public class ObiettivoPrevenzioneCorruzioneTrasparenzaController {
    private final IObiettivoPrevenzioneCorruzioneTrasparenzaService obiettivoPrevenzioneCorruzioneTrasparenzaService;

    public ObiettivoPrevenzioneCorruzioneTrasparenzaController(IObiettivoPrevenzioneCorruzioneTrasparenzaService obiettivoPrevenzioneCorruzioneTrasparenzaService) {
        this.obiettivoPrevenzioneCorruzioneTrasparenzaService = obiettivoPrevenzioneCorruzioneTrasparenzaService;
    }


    @PostMapping("/save")
    public ResponseEntity<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> saveOrUpdate(@RequestBody ObiettivoPrevenzioneCorruzioneTrasparenzaDTO request) {

        return ResponseEntity.ok(obiettivoPrevenzioneCorruzioneTrasparenzaService.saveOrUpdate(request));
    }


    /**
     * Recupera tutti gli obiettivi di corruzione per una Sezione23.
     */
    @GetMapping("/sezione23/{idSezione23}")
    public ResponseEntity<List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO>> getAllBySezione23(@PathVariable Long idSezione23) {
        List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> response = obiettivoPrevenzioneCorruzioneTrasparenzaService.getAllBySezione23(idSezione23);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un obiettivo di performance per ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        obiettivoPrevenzioneCorruzioneTrasparenzaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
