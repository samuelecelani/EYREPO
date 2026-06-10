package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AmpiezzaOrganizzativaDTO;
import it.ey.piao.api.service.IAmpiezzaOrganizzativaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/ampiezza-organizzativa")
public class AmpiezzaOrganizzativaController {

    private final IAmpiezzaOrganizzativaService ampiezzaOrganizzativaService;

    public AmpiezzaOrganizzativaController(IAmpiezzaOrganizzativaService ampiezzaOrganizzativaService) {
        this.ampiezzaOrganizzativaService = ampiezzaOrganizzativaService;
    }

    @GetMapping("/sezione/{idSezione31}")
    public ResponseEntity<List<AmpiezzaOrganizzativaDTO>> findBySezione31(@PathVariable Long idSezione31) {
        return ResponseEntity.ok(ampiezzaOrganizzativaService.findByIdSezione31(idSezione31));
    }

    @PostMapping("/save")
    public ResponseEntity<AmpiezzaOrganizzativaDTO> save(@RequestBody AmpiezzaOrganizzativaDTO request) {
        return ResponseEntity.ok(ampiezzaOrganizzativaService.save(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        ampiezzaOrganizzativaService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
