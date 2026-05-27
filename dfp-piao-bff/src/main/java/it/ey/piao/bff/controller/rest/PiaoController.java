package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ApprovazioneDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.external.PiaoExternalDTO;
import it.ey.piao.bff.service.IPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/piao")
public class PiaoController {

    private final IPiaoService piaoService;

    public PiaoController(IPiaoService piaoService) {
        this.piaoService = piaoService;
    }
//TODO: Esempio di come rendere accessibile l'api su base di un ruolo globale oppure ruolo per singole sezioni
   //@PreAuthorize("hasRole('VALIDATORE') or hasRole('VALIDATORE_' + SEZIONE1)")

    @PostMapping("/initialize")
    public Mono<ResponseEntity<GenericResponseDTO<PiaoDTO>>>initializePiao (
       @RequestBody PiaoDTO piao,
       @RequestParam(required = false) String triennioRiferimento) {
        return piaoService.initializePiao(piao, triennioRiferimento)
            .map(ResponseEntity::ok);
    }


    @GetMapping("/redigi/allowed")
    public Mono<ResponseEntity<GenericResponseDTO<Boolean>>>redigiIsAllowed (@RequestParam  String codPAFK){
        return  piaoService.redigiPiaoIsAllowed(codPAFK).map(ResponseEntity::ok);
    }

    @GetMapping("/tipologia-corrente")
    public Mono<ResponseEntity<GenericResponseDTO<PiaoDTO>>> getTipologiaCorrente(@RequestParam String codPAFK) {
        return piaoService.getTipologiaCorrente(codPAFK).map(ResponseEntity::ok);
    }
    @GetMapping("/findAllPiao")
    public Mono<ResponseEntity<GenericResponseDTO<List<PiaoDTO>>>> findAllPiao (@RequestParam  String codPAFK){
        return  piaoService.findPiaoByCodPAFK(codPAFK).map(ResponseEntity::ok);
    }

    @GetMapping("/precedente")
    public Mono<ResponseEntity<GenericResponseDTO<PiaoDTO>>> findPiaoPrecedente(@RequestParam String codPAFK) {
        return piaoService.findPiaoPrecedente(codPAFK).map(ResponseEntity::ok);
    }

    @PutMapping("/approvazione")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> approvazionePubblicazione(@RequestBody ApprovazioneDTO approvazione)
    {
        return piaoService.pubblicaPiao(approvazione)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/approvazione/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<ApprovazioneDTO>>> getApprovazione(@PathVariable Long idPiao)
    {
        return piaoService.getApprovazione(idPiao).map(ResponseEntity::ok);
    }

    @GetMapping("/findByDenominazioneVersione")
    public Mono<ResponseEntity<GenericResponseDTO<List<PiaoDTO>>>> consultazionePiao(
                                                                                @RequestParam String codPAFK,
                                                                                @RequestParam String denominazione,
                                                                                @RequestParam(required = false) String versione) {
        return piaoService.consultazionePiao(codPAFK, denominazione, versione).map(ResponseEntity::ok);
    }
    @GetMapping("/ultima-versione")
    public Mono<ResponseEntity<GenericResponseDTO<PiaoDTO>>> findPiaoLastVersion(@RequestParam String codPAFK, @RequestParam String denominazione) {
        return piaoService.findPiaoLastVersion(codPAFK, denominazione)
            .map(ResponseEntity::ok);
    }

    /**
     * Endpoint per l'esposizione esterna dei dati PIAO.
     * Recupera tutti i dati relazionati (Anagrafica, OVP, Strategie, Indicatori, AmpiezzaOrganizzativa).
     */
    @GetMapping("/external")
    public Mono<ResponseEntity<GenericResponseDTO<PiaoExternalDTO>>> findPiaoExternal(
            @RequestParam String codPAFK) {
        return piaoService.findPiaoExternal(codPAFK).map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiediValidazione(@PathVariable Long idPiao) {
        return piaoService.richiediValidazione(idPiao).map(ResponseEntity::ok);
    }

    @PatchMapping("/valida-sezione/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long idPiao) {
        return piaoService.validaSezione(idPiao).map(ResponseEntity::ok);
    }

    @PatchMapping("/rifiuta-validazione/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long idPiao, @RequestBody String osservazioni) {
        return piaoService.rifiutaValidazione(idPiao, osservazioni).map(ResponseEntity::ok);
    }

    @PatchMapping("/revoca-validazione/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long idPiao, @RequestBody String osservazioni) {
        return piaoService.revocaValidazione(idPiao, osservazioni).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long idPiao) {
        return piaoService.annullaValidazione(idPiao).map(ResponseEntity::ok);
    }

    @GetMapping("/trienni-riferimento")
    public Mono<GenericResponseDTO<List<String>>> getTrienniRiferimento() {
        return piaoService.getTrienniRiferimento();
    }

    @GetMapping("/pubblicati")
    public Mono<ResponseEntity<GenericResponseDTO<List<PiaoDTO>>>> findAllPiaoPubblicatiByCodePA(
            @RequestParam(required = false) String codPAFK) {
        return piaoService.findAllPiaoPubblicatiByCodePA(codPAFK).map(ResponseEntity::ok);
    }

    @GetMapping("/pubblicati/search")
    public Mono<ResponseEntity<GenericResponseDTO<List<PiaoDTO>>>> searchPubblicati(
            @RequestParam(name = "codiceIpa", required = false) String codiceIpa,
            @RequestParam(name = "tipologia", required = false) String tipologia) {
        return piaoService.searchPubblicati(codiceIpa, tipologia).map(ResponseEntity::ok);
    }

    @GetMapping("/pubblicati/search-by-denominazione")
    public Mono<ResponseEntity<GenericResponseDTO<List<PiaoDTO>>>> searchPubblicatiByDenominazione(
            @RequestParam(name = "denominazione") String denominazione,
            @RequestParam(name = "tipologia", required = false) String tipologia) {
        return piaoService.searchPubblicatiByDenominazione(denominazione, tipologia).map(ResponseEntity::ok);
    }

    @PutMapping("/salva-bozza-pdf")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> salvaInBozzaPiaoPDF(@RequestBody PiaoDTO piao)
    {
        return piaoService.salvaInBozzaPiaoPDF(piao)
            .map(ResponseEntity::ok);
    }

    @PutMapping("/pubblica-pdf")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> pubblicaPiaoPDF(@RequestBody PiaoDTO piao)
    {
        return piaoService.pubblicaPiaoPDF(piao)
            .map(ResponseEntity::ok);
    }
}
