package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StoricoModificaDTO;
import it.ey.enums.Sezione;
import it.ey.piao.bff.service.IStoricoModificaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/storico-modifica")
public class StoricoModificaController {

    private final IStoricoModificaService storicoModificaService;

    public StoricoModificaController(IStoricoModificaService storicoModificaService) {
        this.storicoModificaService = storicoModificaService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<StoricoModificaDTO>>>> getByIdSezioneAndCodTipologiaFK(
            @RequestParam Long idSezione,
            @RequestParam Sezione codTipologiaFK) {
        return storicoModificaService.findByIdSezioneAndCodTipologiaFK(idSezione, codTipologiaFK)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/piao/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<List<StoricoModificaDTO>>>> getByIdPiao(
            @PathVariable Long idPiao) {
        return storicoModificaService.findByIdPiao(idPiao)
            .map(ResponseEntity::ok);
    }
}
