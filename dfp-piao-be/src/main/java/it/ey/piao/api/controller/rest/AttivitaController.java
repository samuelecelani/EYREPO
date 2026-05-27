package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttivitaDTO;
import it.ey.piao.api.service.IAttivitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/attivita")
public class AttivitaController {

    private final IAttivitaService attivitaService;

    public AttivitaController(IAttivitaService attivitaService) {
        this.attivitaService = attivitaService;
    }

    @PostMapping("/save")
    public ResponseEntity<AttivitaDTO> saveOrUpdate(@RequestBody AttivitaDTO dto) {
        AttivitaDTO saved = attivitaService.saveOrUpdate(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/external/{externalId}")
    public ResponseEntity<AttivitaDTO> getByExternalId(@PathVariable Long externalId) {
        AttivitaDTO result = attivitaService.getByExternalId(externalId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/external/{externalId}")
    public ResponseEntity<Void> deleteByExternalId(@PathVariable Long externalId) {
        attivitaService.deleteByExternalId(externalId);
        return ResponseEntity.noContent().build();
    }
}
