package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.UtenteRuoloPaDTO;
import it.ey.piao.api.service.IUtenteRuoloPaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@ApiV1Controller("/utente")
public class UtenteController{

    private final IUtenteRuoloPaService utenteRuoloPaService;

    public UtenteController(IUtenteRuoloPaService utenteRuoloPaService) {
        this.utenteRuoloPaService = utenteRuoloPaService;
    }


    @PostMapping()
    public ResponseEntity<UtenteRuoloPaDTO> create(@RequestBody UtenteRuoloPaDTO utenteRuoloPa) {
        return ResponseEntity.ok(utenteRuoloPaService.create(utenteRuoloPa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        utenteRuoloPaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/codice-pa/{codicePa}")
    public ResponseEntity<List<UtenteRuoloPaDTO>> findByCodicePa(@PathVariable String codicePa) {
        return ResponseEntity.ok(utenteRuoloPaService.findByCodicePa(codicePa));
    }
}
