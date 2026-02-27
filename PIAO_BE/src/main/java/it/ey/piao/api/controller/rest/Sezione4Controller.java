package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.Sezione4DTO;
import it.ey.piao.api.service.ISezione4Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione4")
public class Sezione4Controller {

    private final ISezione4Service sezione4Service;

    public Sezione4Controller(ISezione4Service sezione4Service) {
        this.sezione4Service = sezione4Service;
    }

    @PostMapping("/save")
    public ResponseEntity<Sezione4DTO> save(@RequestBody Sezione4DTO request) {
        return ResponseEntity.ok(sezione4Service.saveOrUpdate(request));
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione4DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione4Service.richiediValidazione(id));
    }
}
