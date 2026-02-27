package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ObbiettivoPerformanceDTO;
import it.ey.enums.TipologiaObbiettivo;
import it.ey.piao.bff.service.IObbiettivoPerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/obbiettivo-performance")
public class ObbiettivoPerformanceController {

    private final IObbiettivoPerformanceService obiettivoPerformanceService;

    public ObbiettivoPerformanceController(IObbiettivoPerformanceService obiettivoPerformanceService) {
        this.obiettivoPerformanceService = obiettivoPerformanceService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<ObbiettivoPerformanceDTO>>> save(@RequestBody ObbiettivoPerformanceDTO request) {
        return obiettivoPerformanceService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione22/{idSezione22}")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>>> getAllBySezione22(@PathVariable Long idSezione22) {
        return obiettivoPerformanceService.getAllBySezione22(idSezione22)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/filter")
    public Mono<ResponseEntity<GenericResponseDTO<List<ObbiettivoPerformanceDTO>>>> findByTipologiaAndFilters(
            @RequestParam TipologiaObbiettivo tipologia,
            @RequestParam(required = false) Long idOvp,
            @RequestParam(required = false) Long idStrategia) {
        return obiettivoPerformanceService.findByTipologiaAndFilters(tipologia, idOvp, idStrategia)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return obiettivoPerformanceService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
