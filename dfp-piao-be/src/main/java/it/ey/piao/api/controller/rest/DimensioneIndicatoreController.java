package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DimensioneIndicatoreDTO;
import it.ey.piao.api.service.IDimensioneIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@ApiV1Controller("/dimensione-indicatore")
public class DimensioneIndicatoreController {

    private final IDimensioneIndicatoreService dimensioneIndicatoreService;

    public DimensioneIndicatoreController(IDimensioneIndicatoreService dimensioneIndicatoreService) {
        this.dimensioneIndicatoreService = dimensioneIndicatoreService;
    }

    @GetMapping
    public ResponseEntity<List<DimensioneIndicatoreDTO>> getDimensioneIndicatore(@RequestParam String codTipologiaFK) {
        return ResponseEntity.ok(dimensioneIndicatoreService.findByCodTipologiaFK(codTipologiaFK));
    }
}
