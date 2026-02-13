package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AdempimentiNormativiDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAdempimentiNormativiService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/adempimenti-normativi")
public class AdempimentiNormativiController
{
    private final IAdempimentiNormativiService adempimentiNormativiService;

    public AdempimentiNormativiController(IAdempimentiNormativiService adempimentiNormativiService)
    {
        this.adempimentiNormativiService = adempimentiNormativiService;
    }

    @PostMapping
    public Mono<GenericResponseDTO<AdempimentiNormativiDTO>> saveOrUpdateAdempimentoNormativo(@RequestBody AdempimentiNormativiDTO adempimentiNormativiDTO)
    {
        return adempimentiNormativiService.saveOrUpdateAdempimento(adempimentiNormativiDTO);
    }

    @DeleteMapping("/{id}")
    public Mono<GenericResponseDTO<Void>> deleteAdempimento(@PathVariable Long id)
    {
        return adempimentiNormativiService.deleteAdempimentoNormativo(id);
    }
}
