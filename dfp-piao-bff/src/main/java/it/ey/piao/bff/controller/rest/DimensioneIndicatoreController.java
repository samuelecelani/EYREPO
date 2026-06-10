package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.DimensioneIndicatoreDTO;
import it.ey.piao.bff.service.IDimensioneIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/dimensione-indicatore")
public class DimensioneIndicatoreController {

    private final IDimensioneIndicatoreService dimensioneIndicatoreService;

    public DimensioneIndicatoreController(IDimensioneIndicatoreService dimensioneIndicatoreService) {
        this.dimensioneIndicatoreService = dimensioneIndicatoreService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<DimensioneIndicatoreDTO>>>> getDimensioneIndicatore(@RequestParam("codTipologiaFK") String codTipologiaFK) {
        return dimensioneIndicatoreService.getDimensioneIndicatore(codTipologiaFK)
            .map(ResponseEntity::ok);
    }
}
