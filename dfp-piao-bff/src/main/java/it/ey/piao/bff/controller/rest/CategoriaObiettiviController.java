package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.dto.CategoriaObiettiviTipDTO;
import it.ey.dto.DimensioneIndicatoreDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.enums.CodTipologiaCategoria;
import it.ey.piao.bff.service.ICategoriaObiettiviService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/categoria-obiettivi")
public class CategoriaObiettiviController {

    private final ICategoriaObiettiviService categoriaObiettiviService;

    public CategoriaObiettiviController(ICategoriaObiettiviService categoriaObiettiviService) {
        this.categoriaObiettiviService = categoriaObiettiviService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<CategoriaObiettiviDTO>>> save(@RequestBody CategoriaObiettiviDTO request) {
        return categoriaObiettiviService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione4/{idSezione4}")
    public Mono<ResponseEntity<GenericResponseDTO<List<CategoriaObiettiviDTO>>>> getAllBySezione4(@PathVariable Long idSezione4) {
        return categoriaObiettiviService.getAllBySezione4(idSezione4)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione) {
        return categoriaObiettiviService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping("/getAllCategoriaObiettivi")
    public Mono<ResponseEntity<GenericResponseDTO<List<CategoriaObiettiviTipDTO>>>> getAllCategoriaObiettivi(@RequestParam CodTipologiaCategoria codTipologiaFK) {
        return categoriaObiettiviService.getAllCategoriaObiettiviTipPerCodTipologiaFK(codTipologiaFK)
            .map(ResponseEntity::ok);
    }
}
