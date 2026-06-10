package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IOrganoPoliticoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@ApiV1Controller("/organo-politico")
public class OrganoPoliticoController {

    private final IOrganoPoliticoService organoPoliticoService;

    public OrganoPoliticoController(IOrganoPoliticoService organoPoliticoService) {
        this.organoPoliticoService = organoPoliticoService;
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> deleteById(
        @PathVariable Long id,
        @RequestParam(required = false) String campiModificati,
        @RequestParam(required = false) Long idPiao,
        @RequestParam(required = false) String testoSezione,
        @RequestParam(required = false) String statoSezione
    ) {
        return organoPoliticoService.deleteById(id, campiModificati, idPiao, testoSezione)
            .map(ResponseEntity::ok);
    }
}
