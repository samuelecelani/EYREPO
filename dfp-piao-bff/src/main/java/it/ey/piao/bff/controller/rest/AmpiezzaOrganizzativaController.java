package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AmpiezzaOrganizzativaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAmpiezzaOrganizzativaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/ampiezza-organizzativa")
public class AmpiezzaOrganizzativaController {

    private final IAmpiezzaOrganizzativaService ampiezzaOrganizzativaService;

    public AmpiezzaOrganizzativaController(IAmpiezzaOrganizzativaService ampiezzaOrganizzativaService) {
        this.ampiezzaOrganizzativaService = ampiezzaOrganizzativaService;
    }

    @GetMapping("/sezione/{idSezione31}")
    public Mono<ResponseEntity<GenericResponseDTO<List<AmpiezzaOrganizzativaDTO>>>> findBySezione31(@PathVariable Long idSezione31) {
        return ampiezzaOrganizzativaService.findByIdSezione31(idSezione31)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<AmpiezzaOrganizzativaDTO>>> save(@RequestBody AmpiezzaOrganizzativaDTO request) {
        return ampiezzaOrganizzativaService.save(request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                  @RequestParam(required = false) String testoSezione,
                                                  @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String campiModificati,
                                                            @RequestParam(required = false) String statoSezione) {
        return ampiezzaOrganizzativaService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
