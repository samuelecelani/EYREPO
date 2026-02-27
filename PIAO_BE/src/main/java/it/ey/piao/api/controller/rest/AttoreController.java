package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AttoreDTO;
import it.ey.enums.Sezione;
import it.ey.piao.api.service.IAttoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/attore")
public class AttoreController {

    private final IAttoreService attoreService;

    public AttoreController(IAttoreService attoreService) {
        this.attoreService = attoreService;
    }

    @GetMapping("/piao/{idPiao}")
    public ResponseEntity<List<AttoreDTO>> findListByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(attoreService.findListByIdPiao(idPiao));
    }

    @PostMapping("/piao/{idPiao}/save")
    public ResponseEntity<AttoreDTO> save(@PathVariable Long idPiao, @RequestBody AttoreDTO attore) {
        return ResponseEntity.ok(attoreService.save(idPiao, attore));
    }

    @GetMapping("/external/{externalId}/sezione/{tipoSezione}")
    public ResponseEntity<AttoreDTO> findByExternalIdAndTipoSezione(
            @PathVariable Long externalId,
            @PathVariable Sezione tipoSezione) {
        AttoreDTO result = attoreService.findByExternalIdAndTipoSezione(externalId, tipoSezione);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
