package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.IndicatoreDTO;
import it.ey.piao.api.service.IIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/indicatore")
public class IndicatoreController {

    private final IIndicatoreService iIndicatoreService;


    public IndicatoreController(IIndicatoreService iIndicatoreService) {
        this.iIndicatoreService = iIndicatoreService;
    }

    @PostMapping("/save")
    public ResponseEntity<IndicatoreDTO> save(@RequestBody IndicatoreDTO request) {
        return ResponseEntity.ok(iIndicatoreService.save(request));
    }

    @GetMapping
    public ResponseEntity<List<IndicatoreDTO>> findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(@RequestParam Long idPiao,
                                                       @RequestParam Long idEntitaFK,
                                                       @RequestParam String codTipologiaFK) {
        return ResponseEntity.ok(iIndicatoreService.findByPiaoIdAndIdEntitaFKAndCodTipologiaFK(idPiao, idEntitaFK, codTipologiaFK));
    }
}
