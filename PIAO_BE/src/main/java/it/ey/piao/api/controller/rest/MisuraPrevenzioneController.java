package it.ey.piao.api.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MisuraPrevenzioneDTO;
import it.ey.piao.api.service.IMisuraPrevenzioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/misura-prevenzione")

public class MisuraPrevenzioneController {
    private final IMisuraPrevenzioneService misuraPrevenzioneService;

    public MisuraPrevenzioneController(IMisuraPrevenzioneService misuraPrevenzioneService) {
        this.misuraPrevenzioneService = misuraPrevenzioneService;
    }



    @PostMapping("/save")
    public ResponseEntity<Void> saveOrUpdate(@RequestBody MisuraPrevenzioneDTO request) {
        misuraPrevenzioneService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }



     // Recupera tutte le MisurePrevenzione, filtrando opzionalmente per Obiettivo o Sezione23.
     // Almeno uno dei due parametri deve essere valorizzato.
    @GetMapping
    public ResponseEntity<List<MisuraPrevenzioneDTO>> getAllByObiettivoPrevenzioneOrBySezione23(@RequestParam(required = false) Long idObiettivoPrevenzione,
                                                                                                @RequestParam(required = false) Long idSezione23) {

        List<MisuraPrevenzioneDTO> response;

        if (idObiettivoPrevenzione != null) {
            response = misuraPrevenzioneService.getAllByObiettivoPrevenzione(idObiettivoPrevenzione);
        } else if (idSezione23 != null) {
            response = misuraPrevenzioneService.getAllBySezione23(idSezione23);
        } else {
            throw new IllegalArgumentException("Bisogna fornire almeno idObiettivoPrevenzione o idSezione23");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un obiettivo di performance per ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        misuraPrevenzioneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
