package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MotivazioneDichiarazioneDTO;
import it.ey.piao.bff.service.IMotivazioneDichiarazioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/motivazione-dichiarazione")
public class MotivazioneDichiarazioneController
{
    private final IMotivazioneDichiarazioneService motivazioneDichiarazioneService;

    public MotivazioneDichiarazioneController(IMotivazioneDichiarazioneService motivazioneDichiarazioneService)
    {
        this.motivazioneDichiarazioneService = motivazioneDichiarazioneService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<MotivazioneDichiarazioneDTO>>> save(@RequestBody MotivazioneDichiarazioneDTO request)
    {
        return motivazioneDichiarazioneService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id)
    {
        return motivazioneDichiarazioneService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<MotivazioneDichiarazioneDTO>>>> getAll()
    {
        return motivazioneDichiarazioneService.getAll()
            .map(ResponseEntity::ok);
    }
}
