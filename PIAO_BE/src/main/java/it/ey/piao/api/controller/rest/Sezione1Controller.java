package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.Sezione21DTO;
import it.ey.piao.api.service.ISezione1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione1")
public class Sezione1Controller {

    private final ISezione1Service sezione1Service;


    public Sezione1Controller(ISezione1Service sezione1Service) {
        this.sezione1Service = sezione1Service;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione1DTO request) {
        sezione1Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione1DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione1Service.richiediValidazione(id));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione1DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione1Service.findByPiao(idPiao));
    }
}
