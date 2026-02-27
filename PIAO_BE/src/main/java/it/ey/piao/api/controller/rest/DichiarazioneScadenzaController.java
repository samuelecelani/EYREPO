package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.piao.api.service.IDichiarazioneScadenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/dichiarazione-scadenza")
public class DichiarazioneScadenzaController
{
    private final IDichiarazioneScadenzaService dichiarazioneScadenzaService;

    public DichiarazioneScadenzaController(IDichiarazioneScadenzaService dichiarazioneScadenzaService) {
        this.dichiarazioneScadenzaService = dichiarazioneScadenzaService;
    }

    @PostMapping
    public ResponseEntity<DichiarazioneScadenzaDTO> save(@RequestBody DichiarazioneScadenzaDTO dichiarazioneScadenzaDTO) {
        return ResponseEntity.ok(dichiarazioneScadenzaService.saveOrUpdate(dichiarazioneScadenzaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dichiarazioneScadenzaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codPAFK}")
    public ResponseEntity<DichiarazioneScadenzaDTO> getExistingDichiarazioneScadenza(@PathVariable String codPAFK)
    {
        return ResponseEntity.ok(dichiarazioneScadenzaService.getExistingDichiarazioneScadenza(codPAFK));
    }
}
