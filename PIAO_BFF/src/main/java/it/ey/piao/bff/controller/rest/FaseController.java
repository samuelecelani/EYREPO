package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.FaseDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IFaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/fase")
public class FaseController {

    private final IFaseService faseService;

    public FaseController(IFaseService faseService) {
        this.faseService = faseService;
    }

    @PostMapping
    public Mono<GenericResponseDTO<FaseDTO>> saveOrUpdateFase(@RequestBody FaseDTO request) {
        return faseService.saveOrUpdateFase(request);
    }

    @DeleteMapping("/{id}")
    public Mono<GenericResponseDTO<Void>> deleteFase(@PathVariable Long id) {
        return faseService.deleteFase(id);
    }
}
