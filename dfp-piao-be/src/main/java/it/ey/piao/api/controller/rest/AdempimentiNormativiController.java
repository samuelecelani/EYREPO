package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AdempimentiNormativiDTO;
import it.ey.piao.api.service.IAdempimentiNormativiService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/adempimenti-normativi")
public class AdempimentiNormativiController
{
    private final IAdempimentiNormativiService adempimentiNormativiService;

    public AdempimentiNormativiController(IAdempimentiNormativiService adempimentiNormativiService)
    {
        this.adempimentiNormativiService = adempimentiNormativiService;
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody AdempimentiNormativiDTO adempimentiNormativiDTO)
    {
        adempimentiNormativiService.saveOrUpdate(adempimentiNormativiDTO);
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

        adempimentiNormativiService.deleteAdempimentoNormativo(id, campiModificati, idPiao,testoSezione,updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
