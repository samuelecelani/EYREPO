package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IIntegrationTeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@ApiV1Controller("/integration-team")
public class IntegrationTeamController {

    private final IIntegrationTeamService integrationTeamService;

    public IntegrationTeamController(IIntegrationTeamService integrationTeamService) {
        this.integrationTeamService = integrationTeamService;
    }


    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> deleteById(
                                                                    @PathVariable Long id,
                                                                    @RequestParam(required = false) String campiModificati,
                                                                    @RequestParam(required = false) Long idPiao,
                                                                    @RequestParam(required = false) String testoSezione,
                                                                    @RequestParam(required = false) String statoSezione) {
        return integrationTeamService.deleteById(id, campiModificati, idPiao, testoSezione)
            .map(response -> ResponseEntity.ok(response));
    }

}
