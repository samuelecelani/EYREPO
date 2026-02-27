package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IDichiarazioneScadenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/dichiarazione-scadenza")
public class DichiarazioneScadenzaController
{
    private final IDichiarazioneScadenzaService dichiarazioneScadenzaService;

    public DichiarazioneScadenzaController(IDichiarazioneScadenzaService dichiarazioneScadenzaService)
    {
        this.dichiarazioneScadenzaService = dichiarazioneScadenzaService;
    }

    @PostMapping
    Mono<ResponseEntity<GenericResponseDTO<DichiarazioneScadenzaDTO>>> save(@RequestBody DichiarazioneScadenzaDTO dto)
    {
        return dichiarazioneScadenzaService.saveOrUpdate(dto)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id)
    {
        return dichiarazioneScadenzaService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/{codPAFK}")
    Mono<ResponseEntity<GenericResponseDTO<DichiarazioneScadenzaDTO>>> getExistingDichiarazioneScadenza(@PathVariable String codPAFK) {
        return dichiarazioneScadenzaService.getExistingDichiarazioneScadenza(codPAFK)
            .map(ResponseEntity::ok);
    }
}
