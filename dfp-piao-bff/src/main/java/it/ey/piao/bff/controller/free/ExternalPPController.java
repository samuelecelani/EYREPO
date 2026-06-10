package it.ey.piao.bff.controller.free;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.external.AmministrazioneExternalPPDTO;
import it.ey.dto.external.DocumentoPiaoExternalPPDTO;
import it.ey.piao.bff.service.IExternalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/external/pp")
public class ExternalPPController {

    private final IExternalService externalService;

    public ExternalPPController(IExternalService externalService) {
        this.externalService = externalService;
    }

    @GetMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<List<DocumentoPiaoExternalPPDTO>>>> getPiaoAndAllegati(
        @RequestParam(required = false) Long idPiao,
        @RequestParam(required = false) String denominazione,
        @RequestParam(required = false) String codePa) {
        return externalService.getPiaoAndAllegati(idPiao, denominazione, codePa).map(ResponseEntity::ok);
    }


    @GetMapping("/amministrazioni")
    public Mono<ResponseEntity<GenericResponseDTO<List<AmministrazioneExternalPPDTO>>>> getAmministrazioni() {
        return externalService.getAmministrazioniServiziComuni(false).map(ResponseEntity::ok);
    }

}
