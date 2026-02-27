package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttoreDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.enums.Sezione;
import it.ey.piao.bff.service.IAttoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/attore")
public class AttoreController {

    private final IAttoreService attoreService;

    public AttoreController(IAttoreService attoreService) {
        this.attoreService = attoreService;
    }

    @GetMapping("/piao/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<List<AttoreDTO>>>> findListByIdPiao(@PathVariable Long idPiao) {
        return attoreService.findListByIdPiao(idPiao)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/piao/{idPiao}/save")
    public Mono<ResponseEntity<GenericResponseDTO<AttoreDTO>>> save(
            @PathVariable Long idPiao,
            @RequestBody AttoreDTO attore) {
        return attoreService.save(idPiao, attore)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/external/{externalId}/sezione/{tipoSezione}")
    public Mono<ResponseEntity<GenericResponseDTO<AttoreDTO>>> findByExternalIdAndTipoSezione(
            @PathVariable Long externalId,
            @PathVariable Sezione tipoSezione) {
        return attoreService.findByExternalIdAndTipoSezione(externalId, tipoSezione)
            .map(ResponseEntity::ok);
    }
}
