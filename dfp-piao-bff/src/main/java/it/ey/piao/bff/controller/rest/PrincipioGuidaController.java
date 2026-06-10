package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.piao.bff.service.IPrincipioGuidaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/principio-guida")
public class PrincipioGuidaController {

    private final IPrincipioGuidaService principioGuidaService;

    public PrincipioGuidaController(IPrincipioGuidaService principioGuidaService) {
        this.principioGuidaService = principioGuidaService;
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String testoSezione,
                                                 @RequestParam(required = false) String statoSezione) {
        return principioGuidaService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
