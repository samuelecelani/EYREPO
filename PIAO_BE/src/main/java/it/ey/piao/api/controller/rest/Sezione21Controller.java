package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;
import it.ey.piao.api.service.ISezione21Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@ApiV1Controller("/sezione21")
public class Sezione21Controller {

    private final ISezione21Service sezione21Service;

    public Sezione21Controller(ISezione21Service sezione21Service) {
        this.sezione21Service = sezione21Service;
    }

    @PostMapping("/piao")
    public ResponseEntity<Sezione21DTO> getOrCreate(@RequestBody PiaoDTO piaoDTO) {
        return ResponseEntity.ok(sezione21Service.getOrCreateSezione21(piaoDTO));
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione21DTO request) {
        sezione21Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione21DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione21Service.richiediValidazione(id));
    }

    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione21DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione21Service.findByIdPiao(idPiao));
    }
}
