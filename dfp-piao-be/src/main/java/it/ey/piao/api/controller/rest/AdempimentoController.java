package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AdempimentoDTO;
import it.ey.piao.api.service.IAdempimentoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@ApiV1Controller("/adempimento")
public class AdempimentoController
{
    private final IAdempimentoService adempimentoService;

    public AdempimentoController(IAdempimentoService adempimentoService)
    {
        this.adempimentoService = adempimentoService;
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody AdempimentoDTO adempimentoDTO)
    {
        adempimentoService.saveOrUpdate(adempimentoDTO);
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
        adempimentoService.deleteAdempimento(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
