package it.ey.piao.api.controller.rest;

import it.ey.dto.AllegatoDTO;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.piao.api.service.IAllegatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allegato")

public class AllegatoController {

private final IAllegatoService allegatoService;

    public AllegatoController(IAllegatoService allegatoService) {
        this.allegatoService = allegatoService;
    }

    @PostMapping("/save")
    public ResponseEntity<AllegatoDTO> saveAllegato(@RequestBody AllegatoDTO allegato){
        return ResponseEntity.ok(allegatoService.insertAllegato(allegato));
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAllegato(@PathVariable("id") Long allegatoId) {
        try {
            allegatoService.deleteAllegato(allegatoId);
            return ResponseEntity.noContent().build(); // HTTP 204
        } catch (Exception ex) {
            return ResponseEntity.status(500).build(); // oppure gestisci con @ControllerAdvice
        }
    }

    @GetMapping("/by-tipologia")
    public ResponseEntity<List<AllegatoDTO>> getAllegatiByTipologia(@RequestParam CodTipologia codTipologia,
                                                                    @RequestParam CodTipologiaAllegato codTipologiaAllegato,
                                                                    @RequestParam Long idPiao
    ) {

        return  ResponseEntity.ok(allegatoService.getAllegatiByTipologiaFK(codTipologia, codTipologiaAllegato,idPiao));
    }

}
