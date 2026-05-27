package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.FaseDTO;
import it.ey.piao.api.service.IFaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@ApiV1Controller("/fase")
public class FaseController {

     private final IFaseService faseService;

    public FaseController(IFaseService faseService) {
        this.faseService = faseService;
    }

    @PostMapping
    public ResponseEntity<Void> save (@RequestBody FaseDTO fase){
        faseService.saveOrUpdateFase(fase);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @RequestParam(required = false) String campiModificati,
                                       @RequestParam(required = false) Long idPiao,
                                       @RequestParam(required = false) String testoSezione,
                                       HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        faseService.deleteFase(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
