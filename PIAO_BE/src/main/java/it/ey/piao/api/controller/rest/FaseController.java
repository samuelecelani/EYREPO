package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.FaseDTO;
import it.ey.piao.api.service.IFaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@ApiV1Controller("/fase")
public class FaseController {

     private final IFaseService faseService;

    public FaseController(IFaseService faseService) {
        this.faseService = faseService;
    }

    @PostMapping
    public ResponseEntity<FaseDTO> save (@RequestBody FaseDTO fase){
        return ResponseEntity.ok(faseService.saveOrUpdateFase(fase));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        faseService.deleteFase(id);
        return ResponseEntity.noContent().build();
    }
}
