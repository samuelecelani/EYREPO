package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AllegatoDTO;
import it.ey.enums.CodTipologia;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.piao.api.service.IAllegatoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/allegato")

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
    public ResponseEntity<Void> deleteAllegato(
            @PathVariable("id") Long allegatoId,
            @RequestParam(required = false, defaultValue = "true") boolean isDoc) {
            allegatoService.deleteAllegato(allegatoId, isDoc);
            return ResponseEntity.noContent().build(); // HTTP 204

    }

    @GetMapping("/by-tipologia")
    public ResponseEntity<List<AllegatoDTO>> getAllegatiByTipologia(@RequestParam CodTipologia codTipologia,
                                                                    @RequestParam CodTipologiaAllegato codTipologiaAllegato,
                                                                    @RequestParam Long idPiao,
                                                                    @RequestParam(required = false,defaultValue = "true") boolean isDoc
    ) {

        return  ResponseEntity.ok(allegatoService.getAllegatiByTipologiaFK(codTipologia, codTipologiaAllegato,idPiao,isDoc));
    }

}
