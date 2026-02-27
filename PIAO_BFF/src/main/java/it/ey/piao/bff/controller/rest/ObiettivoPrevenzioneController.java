package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import it.ey.entity.ObiettivoPrevenzione;
import it.ey.piao.bff.service.IObiettivoPrevenzioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/obiettivo-prevenzione")
public class ObiettivoPrevenzioneController {
    private final IObiettivoPrevenzioneService obiettivoPrevenzioneService;

    public ObiettivoPrevenzioneController(IObiettivoPrevenzioneService obiettivoPrevenzioneService){
        this.obiettivoPrevenzioneService=obiettivoPrevenzioneService;
    }


    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<ObiettivoPrevenzioneDTO>>> save(@RequestBody ObiettivoPrevenzioneDTO request) {
        return obiettivoPrevenzioneService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione23/{idSezione23}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObiettivoPrevenzioneDTO>>>> getAllBySezione23(@PathVariable Long idSezione23) {
        return obiettivoPrevenzioneService.getAllBySezione23(idSezione23)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return obiettivoPrevenzioneService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
