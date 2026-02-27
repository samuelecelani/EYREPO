package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.piao.bff.service.IMisuraPrevenzioneEventoRischioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/misura-prevenzione-evento-rischio")
public class MisuraPrevenzioneEventoRischioController {
    private final IMisuraPrevenzioneEventoRischioService misuraPrevenzioneEventoRischioService;

    public MisuraPrevenzioneEventoRischioController(IMisuraPrevenzioneEventoRischioService misuraPrevenzioneEventoRischioService) {
        this.misuraPrevenzioneEventoRischioService = misuraPrevenzioneEventoRischioService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<MisuraPrevenzioneEventoRischioDTO>>> save(@RequestBody MisuraPrevenzioneEventoRischioDTO request) {
        return misuraPrevenzioneEventoRischioService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/eventoRischio/{idEvento}")
    public Mono<ResponseEntity<GenericResponseDTO<List<MisuraPrevenzioneEventoRischioDTO>>>> getAllByEvento(@PathVariable Long idEventoRischio) {
        return misuraPrevenzioneEventoRischioService.getMisuraPrevenzioneByEventoRischio(idEventoRischio)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return misuraPrevenzioneEventoRischioService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @DeleteMapping("/eventoRischio/{idEventoRischio}")
    public Mono<ResponseEntity<Void>> deleteByEventoRischio(@PathVariable Long idEventoRischio) {
        return misuraPrevenzioneEventoRischioService.deleteByEventoRischio(idEventoRischio)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

}
