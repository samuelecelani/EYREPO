package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttivitaFormativeDTO;
import it.ey.piao.api.service.IAttivitaFormativeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@ApiV1Controller("attivita-formative")
public class AttivitaFormativeController
{
    private final IAttivitaFormativeService attivitaFormativeService;

    public AttivitaFormativeController(IAttivitaFormativeService attivitaFormativeService)
    {
        this.attivitaFormativeService = attivitaFormativeService;
    }

    @PostMapping("/save")
    public ResponseEntity<AttivitaFormativeDTO> saveOrUpdate(@RequestBody AttivitaFormativeDTO request)
    {
        AttivitaFormativeDTO response = attivitaFormativeService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idAttivita}")
    public ResponseEntity<Void> delete(@PathVariable Long idAttivita,
                                       @RequestParam(required = false) String campiModificati,
                                       @RequestParam(required = false) Long idPiao,
                                       @RequestParam(required = false) String testoSezione,
                                       HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        attivitaFormativeService.deleteById(idAttivita, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.ok().build();
    }
}
