package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione23DTO;
import it.ey.piao.api.service.ISezione23Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@ApiV1Controller("/sezione23")
public class Sezione23Controller {

    private final ISezione23Service sezione23Service;

    public Sezione23Controller(ISezione23Service sezione23Service) {
        this.sezione23Service = sezione23Service;
    }

    @PostMapping("/piao")
    public ResponseEntity<Sezione23DTO> getOrCreate(@RequestBody PiaoDTO piaoDTO) {
        return ResponseEntity.ok(sezione23Service.getOrCreateSezione23(piaoDTO));
    }

    @PostMapping("/save")
    public ResponseEntity<Sezione23DTO> save(@RequestBody Sezione23DTO request) {
        return ResponseEntity.ok(sezione23Service.saveOrUpdate(request));
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione23DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione23Service.richiediValidazione(id));
    }
}

