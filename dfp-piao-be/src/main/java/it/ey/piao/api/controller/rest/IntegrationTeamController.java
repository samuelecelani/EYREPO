package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.piao.api.service.IIntegrationTeamService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@ApiV1Controller("/integration-team")
public class IntegrationTeamController {

    private final IIntegrationTeamService integrationTeamService;

    public IntegrationTeamController(IIntegrationTeamService integrationTeamService) {
        this.integrationTeamService = integrationTeamService;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           HttpServletRequest request) {

        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");

        // Delete logica puntuale
        integrationTeamService.deleteById(
            id,
            campiModificati,
            idPiao,
            testoSezione,
            updatedByNameSurname,
            updatedByRole,
            statoSezione
        );

        return ResponseEntity.noContent().build();
    }
}
