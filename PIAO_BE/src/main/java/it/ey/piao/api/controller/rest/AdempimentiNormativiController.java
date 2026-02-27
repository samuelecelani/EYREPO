package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AdempimentiNormativiDTO;
import it.ey.piao.api.service.IAdempimentiNormativiService;
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
    public ResponseEntity<Void> delete(@PathVariable Long id)
    {
        adempimentiNormativiService.deleteAdempimentoNormativo(id);
        return ResponseEntity.noContent().build();
    }
}
