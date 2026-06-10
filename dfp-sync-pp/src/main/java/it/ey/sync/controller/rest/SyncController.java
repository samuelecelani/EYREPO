package it.ey.sync.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import it.ey.sync.annotation.ApiV1Controller;
import it.ey.sync.dto.GenericResponseDTO;
import it.ey.sync.service.SyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sync")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @GetMapping("/piao")
    @Operation(summary = "Sync Piao Pubblicati", description = "Sincronizza i Piao pubblicati dal Portale Piao al Portale Pubblico.")
    public Mono<ResponseEntity<GenericResponseDTO<String>>> syncPiao(@RequestParam(required = false) Long idPiao,
                                                                     @RequestParam(required = false) String denominazione,
                                                                     @RequestParam(required = false) String codePa) {
        return syncService.syncPiao(idPiao,denominazione,codePa).map(ResponseEntity::ok);

    }
}
