package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MisuraPrevenzioneDTO;
import it.ey.dto.ObiettivoPrevenzioneDTO;
import it.ey.piao.bff.service.IMisuraPrevenzioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/misura-prevenzione")
public class MisuraPrevenzioneController {
    private  final IMisuraPrevenzioneService misuraPrevenzioneService;

    public MisuraPrevenzioneController(IMisuraPrevenzioneService misuraPrevenzioneService) {
        this.misuraPrevenzioneService = misuraPrevenzioneService;
    }



    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<MisuraPrevenzioneDTO>>> save(@RequestBody MisuraPrevenzioneDTO request) {
        return misuraPrevenzioneService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<MisuraPrevenzioneDTO>>>>getAllByObiettivoPrevenzioneOrBySezione23(@RequestParam(required = false) Long idObiettivoPrevenzione,
                                                                                                                         @RequestParam(required = false) Long idSezione23)
                                                                                                                        {
    if (idObiettivoPrevenzione != null) {
    return misuraPrevenzioneService.getMisuraPrevenzioneByObiettivoPrevenzione(idObiettivoPrevenzione)
    .map(ResponseEntity::ok);
    } else if (idSezione23 != null) {
    return misuraPrevenzioneService.getAllBySezione23(idSezione23)
    .map(ResponseEntity::ok);
    } else {
    return Mono.error(new IllegalArgumentException("Devi fornire almeno idObiettivoPrevenzione o idSezione23"));
    }
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return misuraPrevenzioneService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
