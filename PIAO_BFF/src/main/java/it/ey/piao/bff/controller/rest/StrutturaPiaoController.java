package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.piao.bff.service.IStrutturaPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/struttura")
public class StrutturaPiaoController {

    private final IStrutturaPiaoService strutturaPiaoService;

    public StrutturaPiaoController(IStrutturaPiaoService strutturaPiaoService) {
        this.strutturaPiaoService = strutturaPiaoService;
    }

    @GetMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<List<StrutturaPiaoDTO>>>> getAllStutturaPiao(
            @RequestParam(required = false) Long idPiao) {
        return strutturaPiaoService.getStrutturaPiao(idPiao).map(ResponseEntity::ok);
    }

    @GetMapping("/validazione")
    public Mono<ResponseEntity<GenericResponseDTO<List<StrutturaValidazioneDTO>>>> getStrutturaValidazione(
            @RequestParam Long idPiao) {
        return strutturaPiaoService.getStrutturaValidazione(idPiao).map(ResponseEntity::ok);
    }
}
