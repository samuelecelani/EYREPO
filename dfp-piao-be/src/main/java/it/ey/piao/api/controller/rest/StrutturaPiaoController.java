package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StrutturaPiaoDTO;
import it.ey.dto.StrutturaValidazioneDTO;
import it.ey.piao.api.service.IStrutturaPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@ApiV1Controller("/struttura")
public class StrutturaPiaoController {

    private final IStrutturaPiaoService strutturaService;

    public StrutturaPiaoController(IStrutturaPiaoService strutturaService) {
        this.strutturaService = strutturaService;
    }

    @GetMapping("/piao")
    public ResponseEntity<List<StrutturaPiaoDTO>> getStruttura(
            @RequestParam(required = false) Long idPiao,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole) {
        return ResponseEntity.ok(strutturaService.getAllStruttura(idPiao, userNameSurname, userRole));
    }

    /**
     * Ritorna le sezioni effettive (foglie) del PIAO: 1, 2.1, 2.2, 2.3, 3.1, 3.2, 3.3.1, 3.3.2, 4
     */
    @GetMapping("/effective")
    public ResponseEntity<List<StrutturaPiaoDTO>> getStrutturaEffective() {
        return ResponseEntity.ok(strutturaService.getAllStrutturaEffective());
    }

    @GetMapping("/validazione")
    public ResponseEntity<List<StrutturaValidazioneDTO>> getStrutturaValidazione(@RequestParam Long idPiao) {
        return ResponseEntity.ok(strutturaService.getAllStrutturaFromValidazione(idPiao));
    }

    @PatchMapping("/validazione/accetta-selezionate")
    public ResponseEntity<Void> accettaValidazioneSezioniSelezionate(@RequestParam Long idPiao,
                                                                     @RequestBody(required = false) Map<String,Long> idSezione) {
        //  Almeno uno dei due deve essere presente
        if (idPiao == null && (idSezione == null || idSezione.isEmpty())) {
            throw new IllegalArgumentException("Bisogna fornire almeno idPiao o la mappa idSezione");
        }

        //  Però per eseguire la validazione  servono entrambi:
        //    - idPiao (per contesto)
        //    - mappa idSezione -> codTipologiaFK
        if (idPiao == null) {
            throw new IllegalArgumentException("idPiao mancante");
        }
        if (idSezione == null || idSezione.isEmpty()) {
            throw new IllegalArgumentException("Nessuna sezione selezionata");
        }

        strutturaService.accettaValidazioneSezioniSelezionate(idPiao, idSezione);
        return ResponseEntity.noContent().build();
    }



}
