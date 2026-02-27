package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AdempimentoDTO;
import it.ey.piao.api.service.IAdempimentoService;
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
    public ResponseEntity<Void> delete(@PathVariable Long id)
    {
        adempimentoService.deleteAdempimento(id);
        return ResponseEntity.noContent().build();
    }
}
