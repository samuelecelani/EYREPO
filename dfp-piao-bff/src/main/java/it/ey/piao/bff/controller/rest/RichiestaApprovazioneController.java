package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.RichiestaApprovazioneDTO;

import it.ey.piao.bff.service.IRichiestaApprovazioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/richiesta-approvazione")
public class RichiestaApprovazioneController {


    private final IRichiestaApprovazioneService richiestaApprovazioneService;

    public RichiestaApprovazioneController(IRichiestaApprovazioneService richiestaApprovazioneService) {
        this.richiestaApprovazioneService = richiestaApprovazioneService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody RichiestaApprovazioneDTO request) {
        return richiestaApprovazioneService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }


    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<RichiestaApprovazioneDTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return richiestaApprovazioneService.findByPiao(idPiao).map(ResponseEntity::ok);
    }

}
