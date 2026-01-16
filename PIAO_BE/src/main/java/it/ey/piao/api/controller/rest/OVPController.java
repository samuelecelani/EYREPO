package it.ey.piao.api.controller.rest;

import it.ey.dto.OVPDTO;
import it.ey.piao.api.service.IOVPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ovp")
public class OVPController {

    private final IOVPService ovpService;

    public OVPController(IOVPService ovpService) {
        this.ovpService = ovpService;
    }

    @PostMapping("/save")
    public ResponseEntity<OVPDTO> save(@RequestBody OVPDTO request) {
        return ResponseEntity.ok(ovpService.saveOrUpdate(request));
    }

    @GetMapping("/sezione/{idSezione}")
    public ResponseEntity<List<OVPDTO>> getAllBySezione(@PathVariable Long idSezione) {
        return ResponseEntity.ok(ovpService.findAllOVPBySezione(idSezione));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ovpService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
