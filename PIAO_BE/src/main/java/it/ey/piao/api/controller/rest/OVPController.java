package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.OVPDTO;
import it.ey.dto.OVPMatriceDataDTO;
import it.ey.piao.api.service.IOVPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/ovp")
public class OVPController {

    private final IOVPService ovpService;

    public OVPController(IOVPService ovpService) {
        this.ovpService = ovpService;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody OVPDTO request) {
        ovpService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sezione/{idSezione}")
    public ResponseEntity<List<OVPDTO>> getAllBySezione(@PathVariable Long idSezione) {
        return ResponseEntity.ok(ovpService.findAllOVPBySezione(idSezione));
    }

    @GetMapping("/piao/{piaoId}")
    public ResponseEntity<List<OVPDTO>> getAllByPiao(@PathVariable Long piaoId) {
        return ResponseEntity.ok(ovpService.findAllOVPByPiao(piaoId));
    }

    @GetMapping("/matrice-data")
    public ResponseEntity<OVPMatriceDataDTO> getMatriceData(
            @RequestParam(required = false) Long idSezione,
            @RequestParam Long idSezione1,
            @RequestParam(required = false) Long idPiao) {
        return ResponseEntity.ok(ovpService.findOVPMatriceData(idSezione, idSezione1, idPiao));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ovpService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
