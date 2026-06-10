package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.UtenteRuoliPaSezioneDTO;
import it.ey.piao.api.service.IUtenteRuoloPaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/utente")
public class UtenteController {

    private final IUtenteRuoloPaService utenteRuoloPaService;

    public UtenteController(IUtenteRuoloPaService utenteRuoloPaService) {
        this.utenteRuoloPaService = utenteRuoloPaService;
    }

    /**
     * Salva le sezioni associate a un utente esterno per una specifica amministrazione.
     * Richiede sempre: externalUserId + idAmministrazione + sezioni.
     */
    @PostMapping("/{externalUserId}/sezioni")
    public ResponseEntity<List<UtenteRuoliPaSezioneDTO>> saveSezioni(
            @PathVariable String externalUserId,
            @RequestParam String idAmministrazione,
            @RequestBody List<UtenteRuoliPaSezioneDTO> sezioni) {
        return ResponseEntity.ok(utenteRuoloPaService.saveSezioni(externalUserId, idAmministrazione, sezioni));
    }

    /**
     * Recupera le sezioni associate a un utente esterno per una specifica amministrazione.
     */
    @GetMapping("/{externalUserId}/sezioni")
    public ResponseEntity<List<UtenteRuoliPaSezioneDTO>> getSezioni(
            @PathVariable String externalUserId,
            @RequestParam String idAmministrazione) {
        return ResponseEntity.ok(utenteRuoloPaService.findSezioniByExternalUserIdAndIdAmministrazione(externalUserId, idAmministrazione));
    }

    /**
     * Recupera tutti gli utenti/sezioni per una data amministrazione.
     */
    @GetMapping("/amministrazione/{idAmministrazione}/sezioni")
    public ResponseEntity<List<UtenteRuoliPaSezioneDTO>> getSezioniByAmministrazione(
            @PathVariable String idAmministrazione) {
        return ResponseEntity.ok(utenteRuoloPaService.findSezioniByIdAmministrazione(idAmministrazione));
    }

    /**
     * Elimina tutte le sezioni associate a un utente esterno per una specifica amministrazione.
     */
    @DeleteMapping("/{externalUserId}/sezioni")
    public ResponseEntity<Void> deleteSezioni(
            @PathVariable String externalUserId,
            @RequestParam String idAmministrazione) {
        utenteRuoloPaService.deleteSezioniByExternalUserIdAndIdAmministrazione(externalUserId, idAmministrazione);
        return ResponseEntity.noContent().build();
    }
}
