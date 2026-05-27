package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.TabellaFunzionaleDTO;
import it.ey.piao.bff.service.ITabellaFunzionaleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/tabella-funzionale")
public class TabellaFunzionaleController {

    private final ITabellaFunzionaleService tabellaFunzionaleService;

    public TabellaFunzionaleController(ITabellaFunzionaleService tabellaFunzionaleService) {
        this.tabellaFunzionaleService = tabellaFunzionaleService;
    }

    @GetMapping("/sezione/{codTipologiaFK}/{idEntitaFK}")
    public Mono<ResponseEntity<GenericResponseDTO<List<TabellaFunzionaleDTO>>>> findByEntitaAndTipologia(
            @PathVariable String codTipologiaFK,
            @PathVariable Long idEntitaFK) {
        return tabellaFunzionaleService.findByIdEntitaFKAndCodTipologiaFK(idEntitaFK, codTipologiaFK)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<TabellaFunzionaleDTO>>> save(@RequestBody TabellaFunzionaleDTO request) {
        return tabellaFunzionaleService.save(request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                  @RequestParam(required = false) String campiModificati,
                                                  @RequestParam(required = false) Long idPiao,
                                                  @RequestParam(required = false) String codTipologiaFK,
                                                  @RequestParam(required = false) String testoSezione,
                                                  @RequestParam(required = false) String statoSezione) {
        return tabellaFunzionaleService.deleteById(id, campiModificati, idPiao, codTipologiaFK, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
