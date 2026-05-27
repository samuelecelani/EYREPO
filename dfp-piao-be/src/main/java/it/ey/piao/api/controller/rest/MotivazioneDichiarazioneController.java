package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MotivazioneDichiarazioneDTO;
import it.ey.piao.api.service.IMotivazioneDichiarazioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/motivazione-dichiarazione")
public class MotivazioneDichiarazioneController
{
    private final IMotivazioneDichiarazioneService motivazioneDichiarazioneService;

    public MotivazioneDichiarazioneController(IMotivazioneDichiarazioneService motivazioneDichiarazioneService)
    {
        this.motivazioneDichiarazioneService = motivazioneDichiarazioneService;
    }

    @PostMapping
    public ResponseEntity<MotivazioneDichiarazioneDTO> save(@RequestBody MotivazioneDichiarazioneDTO dto)
    {
        return ResponseEntity.ok(motivazioneDichiarazioneService.saveOrUpdate(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id)
    {
        motivazioneDichiarazioneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MotivazioneDichiarazioneDTO>> getAll()
    {
        return ResponseEntity.ok(motivazioneDichiarazioneService.findAll());
    }
}
