package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AnagraficaDTO;
import it.ey.piao.api.service.IAnagraficaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/anagrafica")
public class AnagraficaController {

    private final IAnagraficaService anagraficaService;

    public AnagraficaController(IAnagraficaService anagraficaService) {
        this.anagraficaService = anagraficaService;
    }

    @GetMapping
    public ResponseEntity<List<AnagraficaDTO>> getAll() {
        return ResponseEntity.ok(anagraficaService.getAll());
    }

    @GetMapping("/by-piao/{idPiao}")
    public ResponseEntity<AnagraficaDTO> findByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(anagraficaService.findByIdPiao(idPiao));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AnagraficaDTO>> search(
            @RequestParam(name = "codiceIpa", required = false) String codiceIpa,
            @RequestParam(name = "tipologia", required = false) String tipologia,
            @RequestParam(name = "denominazione", required = false) String denominazione) {
        return ResponseEntity.ok(anagraficaService.search(codiceIpa, tipologia, denominazione));
    }

    @GetMapping("/tipologie")
    public ResponseEntity<List<String>> getTipologie() {
        return ResponseEntity.ok(anagraficaService.getTipologie());
    }

    @PostMapping("/save")
    public ResponseEntity<AnagraficaDTO> save(@RequestBody AnagraficaDTO anagraficaDTO) {
        return ResponseEntity.ok(anagraficaService.save(anagraficaDTO));
    }
}
