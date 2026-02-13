package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DatiPubblicatiDTO;
import it.ey.piao.api.service.IDatiPubblicatiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/dati-pubblicati")

public class DatiPubblicatiController {
    private final IDatiPubblicatiService datiPubblicatiService;
    private static final Logger log = LoggerFactory.getLogger(DatiPubblicatiController.class);


    public DatiPubblicatiController(IDatiPubblicatiService datiPubblicatiService) {
        this.datiPubblicatiService = datiPubblicatiService;
    }


    @PostMapping("/save")
    public ResponseEntity<DatiPubblicatiDTO> saveOrUpdate(@RequestBody DatiPubblicatiDTO request) {
        DatiPubblicatiDTO response = datiPubblicatiService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/obbligo-legge/{obbligoLegge}")
    public ResponseEntity<List<DatiPubblicatiDTO>> getAllByObbligoLegge(@PathVariable Long obbligoLegge) {
        List<DatiPubblicatiDTO> response = datiPubblicatiService.getAllByObbligoLeggeId(obbligoLegge);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        datiPubblicatiService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
