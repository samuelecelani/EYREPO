package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.*;
import it.ey.piao.bff.service.IObiettiviRisultatiFotografiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/obiettivi-risultati-fotografia")
public class ObiettiviRisultatiFotografiaController {
    private final IObiettiviRisultatiFotografiaService obiettiviRisultatiFotografiaService;

    public ObiettiviRisultatiFotografiaController(IObiettiviRisultatiFotografiaService obiettiviRisultatiFotografiaService)
    {
        this.obiettiviRisultatiFotografiaService = obiettiviRisultatiFotografiaService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<ObiettiviRisultatiFotografiaDTO>>> save(@RequestBody ObiettiviRisultatiFotografiaDTO request)
    {
        return obiettiviRisultatiFotografiaService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/obiettivi-risultati/{idSezione332}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>>>> getObiettiviSezione332(@PathVariable Long idSezione332)
    {
        return obiettiviRisultatiFotografiaService.getObiettiviRisultatiBySezione332(idSezione332)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/fotografia-formazione/{idSezione332}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObiettiviRisultatiFotografiaDTO>>>> getFotografieSezione332(@PathVariable Long idSezione332)
    {
        return obiettiviRisultatiFotografiaService.getFotografieFormazioneBySezione332(idSezione332)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                  @RequestParam(required = false) String testoSezione,
                                                  @RequestParam(required = false) String statoSezione)
    {
        return obiettiviRisultatiFotografiaService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}








