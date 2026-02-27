package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;
import it.ey.dto.Sezione22DTO;
import it.ey.piao.api.service.ISezione22Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@ApiV1Controller("/sezione22")
public class Sezione22Controller {

    private final ISezione22Service sezione22Service;

    public Sezione22Controller(ISezione22Service sezione22Service) {
        this.sezione22Service = sezione22Service;
    }

    @PostMapping("/piao")
    public ResponseEntity<Sezione22DTO> getOrCreate(@RequestBody PiaoDTO piaoDTO) {
        return ResponseEntity.ok(sezione22Service.getOrCreateSezione22(piaoDTO));
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione22DTO request) {
        sezione22Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione22DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione22Service.richiediValidazione(id));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione22DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione22Service.findByIdPiao(idPiao));
    }
}

