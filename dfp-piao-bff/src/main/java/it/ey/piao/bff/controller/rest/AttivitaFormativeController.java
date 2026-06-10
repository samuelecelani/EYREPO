package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttivitaFormativeDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAttivitaFormativeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("attivita-formative")
public class AttivitaFormativeController
{
    private final IAttivitaFormativeService attivitaFormativeService;

    public AttivitaFormativeController(IAttivitaFormativeService attivitaFormativeService)
    {
        this.attivitaFormativeService = attivitaFormativeService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<AttivitaFormativeDTO>>> save(@RequestBody AttivitaFormativeDTO request)
    {
        return attivitaFormativeService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione)
    {
        return attivitaFormativeService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
