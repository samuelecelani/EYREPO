package it.ey.piao.api.controller.rest;


import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione23DTO;
import it.ey.dto.Sezione331DTO;
import it.ey.piao.api.service.ISezione331Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("/sezione331")
public class Sezione331Controller {

    private final ISezione331Service sezione331Service;

    public Sezione331Controller(ISezione331Service sezione331Service) {
        this.sezione331Service = sezione331Service;
    }


    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody Sezione331DTO request) {
        sezione331Service.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/validazione/{id}")
    public ResponseEntity<Sezione331DTO> richiediValidazione(@PathVariable Long id) {
        return ResponseEntity.ok(sezione331Service.richiediValidazione(id));
    }
    @GetMapping("/{idPiao}")
    public ResponseEntity<Sezione331DTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(sezione331Service.findByPiao(idPiao));
    }

}
