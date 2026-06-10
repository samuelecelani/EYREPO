package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AmministrazioneInternalDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAmministrazioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/amministrazione")
public class AmministrazioneController {

    private final IAmministrazioneService amministrazioneService;

    @Autowired
    public AmministrazioneController(IAmministrazioneService amministrazioneService) {
        this.amministrazioneService = amministrazioneService;
    }

    @GetMapping("/search")
    public Mono<ResponseEntity<GenericResponseDTO<List<AmministrazioneInternalDTO>>>> search(
            @RequestParam(name = "codiceIpa", required = false) String codiceIpa,
            @RequestParam(name = "tipologia", required = false) String tipologia,
            @RequestParam(name = "denominazione", required = false) String denominazione) {

        return amministrazioneService.search(codiceIpa, tipologia, denominazione)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping("/tipologie")
    public Mono<ResponseEntity<GenericResponseDTO<List<String>>>> getTipologie() {
        return amministrazioneService.getTipologie()
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}
