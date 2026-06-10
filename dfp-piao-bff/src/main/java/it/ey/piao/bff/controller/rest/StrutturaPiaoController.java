package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.piao.bff.service.IStrutturaPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

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

    @PatchMapping("/validazione/accetta-selezionate")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> accettaValidazioneSezioniSelezionate(@RequestParam Long idPiao,
                                                                                               @RequestBody Map<String,Long> idSezione) {

        return strutturaPiaoService.accettaValidazioneSezioniSelezionate(idPiao, idSezione).map(ResponseEntity::ok);
    }

}
