package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.IndicatoreDTO;
import it.ey.piao.bff.service.IIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/indicatore")
public class IndicatoreController {

    private final IIndicatoreService iIndicatoreService;


    public IndicatoreController(IIndicatoreService iIndicatoreService) {
        this.iIndicatoreService = iIndicatoreService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<IndicatoreDTO>>> save(@RequestBody IndicatoreDTO request) {
        return iIndicatoreService.saveOrUpdate(request).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<IndicatoreDTO>>>> findBy(@RequestParam Long idPiao,
                                                                                  @RequestParam Long idEntitaFK,
                                                                                  @RequestParam String codTipologiaFK) {
        return iIndicatoreService.findBy(idPiao, idEntitaFK, codTipologiaFK).map(ResponseEntity::ok);
    }

}

