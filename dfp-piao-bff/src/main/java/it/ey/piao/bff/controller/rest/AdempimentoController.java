package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AdempimentoDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAdempimentoService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/adempimento")
public class AdempimentoController
{
    private final IAdempimentoService adempimentoService;

    public AdempimentoController(IAdempimentoService adempimentoService) { this.adempimentoService = adempimentoService; }

        @PostMapping
        public Mono<GenericResponseDTO<AdempimentoDTO>> saveOrUpdateAdempimento(@RequestBody AdempimentoDTO adempimentoDTO)
        {
            return adempimentoService.saveOrUpdateAdempimento(adempimentoDTO);
        }

    @DeleteMapping("/{id}")
    public Mono<GenericResponseDTO<Void>> deleteAdempimento(@PathVariable Long id,
                                                            @RequestParam(required = false) String campiModificati,
                                                            @RequestParam(required = false) Long idPiao,
                                                            @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione)
    {
        return adempimentoService.deleteAdempimento(id, campiModificati, idPiao, testoSezione);
    }
}
