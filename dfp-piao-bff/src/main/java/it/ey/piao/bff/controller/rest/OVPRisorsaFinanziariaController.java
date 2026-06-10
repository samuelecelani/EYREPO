package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IOVPRisorsaFinanziariaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@ApiV1Controller("/ovp-risorse-finanziarie")
public class OVPRisorsaFinanziariaController {

    private final IOVPRisorsaFinanziariaService ovpRisorsaFinanziariaService;

    public OVPRisorsaFinanziariaController(IOVPRisorsaFinanziariaService ovpRisorsaFinanziariaService) {
        this.ovpRisorsaFinanziariaService = ovpRisorsaFinanziariaService;
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> deleteById(
                                                                    @PathVariable Long id,
                                                                    @RequestParam(required = false) String campiModificati,
                                                                    @RequestParam(required = false) Long idPiao,
                                                                    @RequestParam(required = false) String testoSezione,
                                                                    @RequestParam(required = false) String statoSezione) {
        return ovpRisorsaFinanziariaService.deleteById(id, campiModificati, idPiao, testoSezione)
            .map(ResponseEntity::ok);
    }
}

