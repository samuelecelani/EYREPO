package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.Sezione31DTO;
import it.ey.piao.api.service.ISezione31Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione31")
public class Sezione31Controller
{
    private final ISezione31Service sezione31Service;

    public Sezione31Controller(ISezione31Service sezione31Service) {
        this.sezione31Service = sezione31Service;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione31DTO request)
    {
        sezione31Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione31DTO> richiediValidazione(@PathVariable Long id)
    {
        return ResponseEntity.ok(sezione31Service.richiediValidazione(id));
    }

    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione31DTO> getByIdPiao(@PathVariable Long idPiao)
    {
        return ResponseEntity.ok(sezione31Service.findByPiao(idPiao));
    }
}
