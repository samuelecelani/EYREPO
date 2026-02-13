package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.NovitaDTO;
import it.ey.piao.bff.service.INovitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/novita")
public class NovitaController {

    private final INovitaService novitaService;

    public NovitaController(INovitaService novitaService) {
        this.novitaService = novitaService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<NovitaDTO>>>> getNovita(
        @RequestParam String modulo) {
        return novitaService.getNovita(modulo)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}

