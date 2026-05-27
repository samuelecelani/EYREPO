package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StoricoModificaDTO;
import it.ey.enums.Sezione;
import it.ey.piao.api.service.IStoricoModificaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiV1Controller("/storico-modifica")
public class StoricoModificaController {

    private final IStoricoModificaService storicoModificaService;

    public StoricoModificaController(IStoricoModificaService storicoModificaService) {
        this.storicoModificaService = storicoModificaService;
    }

    @GetMapping
    public ResponseEntity<List<StoricoModificaDTO>> getByIdSezioneAndCodTipologiaFK(
            @RequestParam Long idSezione,
            @RequestParam Sezione codTipologiaFK) {
        return ResponseEntity.ok(storicoModificaService.findByIdSezioneAndCodTipologiaFK(idSezione, codTipologiaFK));
    }

    @GetMapping("/piao/{idPiao}")
    public ResponseEntity<List<StoricoModificaDTO>> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(storicoModificaService.findByIdPiao(idPiao));
    }
}
