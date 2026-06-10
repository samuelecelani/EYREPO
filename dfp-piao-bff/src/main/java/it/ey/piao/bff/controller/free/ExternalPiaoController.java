package it.ey.piao.bff.controller.free;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.external.PiaoExternalDTO;
import it.ey.piao.bff.service.IPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/external/piao")
public class ExternalPiaoController {

    private final IPiaoService piaoService;

    public ExternalPiaoController(IPiaoService piaoService) {
        this.piaoService = piaoService;
    }

    /**
     * Endpoint per l'esposizione esterna dei dati PIAO.
     * Recupera tutti i dati relazionati (Anagrafica, OVP, Strategie, Indicatori, AmpiezzaOrganizzativa).
     */
    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<PiaoExternalDTO>>> findPiaoExternal(
            @RequestParam String codPAFK) {
        return piaoService.findPiaoExternal(codPAFK)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}

