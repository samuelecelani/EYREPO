package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.RuoloDTO;
import it.ey.piao.api.service.RuoloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@ApiV1Controller("/ruolo")
public class RuoloController {

    private final RuoloService ruoloService;

    public RuoloController(RuoloService ruoloService) {
        this.ruoloService = ruoloService;
    }

    @GetMapping("/findByTipologia")
    public ResponseEntity<List<RuoloDTO>> getAllegatiByTipologia(@RequestParam List<String> tipologia
    ) {
        return  ResponseEntity.ok(ruoloService.findByTipologia(tipologia));
    }
}
