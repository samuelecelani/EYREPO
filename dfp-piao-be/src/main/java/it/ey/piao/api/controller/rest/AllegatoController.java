package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AllegatoDTO;
import it.ey.enums.CodTipologiaAllegato;
import it.ey.enums.Sezione;
import it.ey.piao.api.service.IAllegatoService;
import jakarta.servlet.http.HttpServletRequest;
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
            @RequestParam(required = false, defaultValue = "true") boolean isDoc,
            @RequestParam(required = false) String campiModificati,
            @RequestParam(required = false) Long idPiao,
            @RequestParam(required = false) String codTipologiaFK,
            @RequestParam(required = false) String testoSezione,
            HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
            allegatoService.deleteAllegato(allegatoId, isDoc, campiModificati, idPiao, codTipologiaFK, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
            return ResponseEntity.noContent().build(); // HTTP 204

    }

    @GetMapping("/by-tipologia")
    public ResponseEntity<List<AllegatoDTO>> getAllegatiByTipologia(@RequestParam List<Sezione> codTipologia,
                                                                    @RequestParam List<CodTipologiaAllegato> codTipologiaAllegato,
                                                                    @RequestParam Long idPiao,
                                                                    @RequestParam(required = false,defaultValue = "true") boolean isDoc
    ) {

        return  ResponseEntity.ok(allegatoService.getAllegatiByTipologiaFK(codTipologia, codTipologiaAllegato,idPiao,isDoc));
    }

    @GetMapping("/findByIdPiao")
    public ResponseEntity<List<AllegatoDTO>> findByIdPiao(@RequestParam Long idPiao){
        return ResponseEntity.ok(allegatoService.findByIdPiao(idPiao));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllegatoDTO> findById(@PathVariable("id") Long id) {
        AllegatoDTO allegato = allegatoService.findById(id);
        if (allegato == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(allegato);
    }

    @PutMapping("/update")
    public ResponseEntity<AllegatoDTO> updateAllegato(@RequestBody AllegatoDTO allegato) {
        return ResponseEntity.ok(allegatoService.updateAllegato(allegato));
    }

    @DeleteMapping("/delete-bozza")
    public ResponseEntity<Void> deleteBozza(@RequestParam String codDocumento) {
        allegatoService.deleteBozzaByCodDocumento(codDocumento);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

}
