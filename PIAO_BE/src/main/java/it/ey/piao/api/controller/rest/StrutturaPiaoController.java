package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.piao.api.service.IStrutturaPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiV1Controller("/struttura")
public class StrutturaPiaoController {

    private final IStrutturaPiaoService strutturaService;

    public StrutturaPiaoController(IStrutturaPiaoService strutturaService) {
        this.strutturaService = strutturaService;
    }

    @GetMapping("/piao")
    public ResponseEntity<List<StrutturaPiaoDTO>> getStruttura(@RequestParam(required = false) Long idPiao) {
        return ResponseEntity.ok(strutturaService.getAllStruttura(idPiao));
    }

    @GetMapping("/validazione")
    public ResponseEntity<List<StrutturaValidazioneDTO>> getStrutturaValidazione(@RequestParam Long idPiao) {
        return ResponseEntity.ok(strutturaService.getAllStrutturaFromValidazione(idPiao));
    }
}
