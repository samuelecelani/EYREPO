package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.*;
import it.ey.piao.bff.service.IStatoPIAOService;
import it.ey.piao.bff.service.IStatoSezioneService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/stato")
public class StatoController {


    private final IStatoPIAOService statoPIAOService;
    private final IStatoSezioneService statoSezioneService;

    public StatoController(IStatoPIAOService statoPIAOService, IStatoSezioneService statoSezioneService) {
        this.statoPIAOService = statoPIAOService;
        this.statoSezioneService = statoSezioneService;
    }


    @GetMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<List<StatoPIAODTO>>>> getStatoPiao() {
        return statoPIAOService.getStatoPIAO()
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
    @GetMapping("/sezione")
    public Mono<ResponseEntity<GenericResponseDTO<List<StatoSezioneDTO>>>> getStatoSezione() {
        return statoSezioneService.getStatoSezione()
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
