package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.piao.bff.service.IPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/piao")
public class PiaoController {

    private final IPiaoService piaoService;

    public PiaoController(IPiaoService piaoService) {
        this.piaoService = piaoService;
    }
//TODO: Esempio di come rendere accessibile l'api su base di un ruolo globale oppure ruolo per singole sezioni
   //@PreAuthorize("hasRole('VALIDATORE') or hasRole('VALIDATORE_' + SEZIONE1)")

    @PostMapping("/initialize")
    public Mono<ResponseEntity<GenericResponseDTO<PiaoDTO>>>initializePiao (
       @RequestBody PiaoDTO piao) {
        return piaoService.initializePiao(piao)
            .map(ResponseEntity::ok);
    }


    @GetMapping("/redigi/allowed")
    public Mono<ResponseEntity<GenericResponseDTO<Boolean>>>redigiIsAllowed (@RequestParam  String codPAFK){
        return  piaoService.redigiPiaoIsAllowed(codPAFK).map(ResponseEntity::ok);
    }
    @GetMapping("/findAllPiao")
    public Mono<ResponseEntity<GenericResponseDTO<List<PiaoDTO>>>> findAllPiao (@RequestParam  String codPAFK){
        return  piaoService.findPiaoByCodPAFK(codPAFK).map(ResponseEntity::ok);
    }
}
