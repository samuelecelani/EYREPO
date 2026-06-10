package it.ey.piao.bff.controller.rest;


import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DatiPubblicatiDTO;
import it.ey.dto.GenericResponseDTO;

import it.ey.piao.bff.service.IDatiPubblicatiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/dati-pubblicati")
public class DatiPubblicatiController {
    private final IDatiPubblicatiService datiPubblicatiService;

    public DatiPubblicatiController(IDatiPubblicatiService datiPubblicatiService) {
        this.datiPubblicatiService = datiPubblicatiService;
    }


    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<DatiPubblicatiDTO>>> save(@RequestBody DatiPubblicatiDTO request) {
        return datiPubblicatiService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/obbligo-legge/{idObbligoLegge}")
    public Mono<ResponseEntity<GenericResponseDTO<List<DatiPubblicatiDTO>>>> getAllByObbligoLegge(@PathVariable Long idObbligoLegge) {
        return datiPubblicatiService.getAllByObbligoLegge(idObbligoLegge)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione) {
        return datiPubblicatiService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
