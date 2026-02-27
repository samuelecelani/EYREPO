package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione22DTO;
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


    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione23DTO request) {
        sezione23Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione23DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione23Service.richiediValidazione(id));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione23DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione23Service.findByPiao(idPiao));
    }
}

