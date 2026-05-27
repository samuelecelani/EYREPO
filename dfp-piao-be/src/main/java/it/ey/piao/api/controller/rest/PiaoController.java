package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ApprovazioneDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.external.DocumentoPiaoExternalPPDTO;
import it.ey.dto.external.PiaoExternalDTO;
import it.ey.piao.api.service.IPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiV1Controller("/piao")
@RequiredArgsConstructor
public class PiaoController {

    private final IPiaoService piaoService;

    @PostMapping("/getOrCreate")
    public ResponseEntity<PiaoDTO> getOrCreate(@RequestBody PiaoDTO dto,
                                               @RequestParam(required = false) String triennioRiferimento) {
        PiaoDTO result = piaoService.getOrCreatePiao(dto, triennioRiferimento);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/redigiPiaoIsAllowed")
    public ResponseEntity<Boolean> getOrCreate(@RequestParam String codPAFK) {
        return ResponseEntity.ok(piaoService.redigiPiaoIsAllowed( codPAFK));
    }

    @GetMapping("/tipologia-corrente")
    public ResponseEntity<PiaoDTO> getTipologiaCorrente(@RequestParam String codPAFK) {
        PiaoDTO result = piaoService.getTipologiaCorrente(codPAFK);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/findById")
    public ResponseEntity<PiaoDTO> findById(@RequestParam Long id) {
        PiaoDTO result = piaoService.findById(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/findBycodPAFK")
    public ResponseEntity<List<PiaoDTO>> findByCodPAFK(@RequestParam String codPAFK) {
        return ResponseEntity.ok(piaoService.getAllPiaoByCodPAFK( codPAFK));
    }

    @GetMapping("/precedente")
    public ResponseEntity<PiaoDTO> findPiaoPrecedente(@RequestParam String codPAFK) {
        return ResponseEntity.ok(piaoService.findPiaoPrecedente(codPAFK));
    }

    @PutMapping("/approvazione")
    public ResponseEntity<Void> approvazionePubblicazione(@RequestBody ApprovazioneDTO approvazione)
    {
        piaoService.pubblicaPiao(approvazione);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/approvazione/{idPiao}")
    public ResponseEntity<ApprovazioneDTO> getApprovazione(@PathVariable Long idPiao)
    {
        return ResponseEntity.ok(piaoService.getApprovazione(idPiao));
    }


    @GetMapping("/findByDenominazioneVersione")
    public ResponseEntity<List<PiaoDTO>> consultazionePiao(@RequestParam String codPAFK,
                                                     @RequestParam String denominazione,
                                                     @RequestParam(required = false) String versione) {
        List<PiaoDTO> result= piaoService.findByCodPafkAndDenominazioneAndVersione(codPAFK, denominazione, versione);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/ultima-versione")
    public ResponseEntity<PiaoDTO> getPiaoLastVersion (@RequestParam String codPAFK, @RequestParam String denominazione) {
        PiaoDTO result = piaoService.findPiaoLastVersion(codPAFK, denominazione);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint per l'esposizione esterna dei dati PIAO.
     * Recupera tutti i dati relazionati (Anagrafica, OVP, Strategie, Indicatori, AmpiezzaOrganizzativa).
     */
    @GetMapping("/external")
    public ResponseEntity<PiaoExternalDTO> findPiaoExternal(@RequestParam String codPAFK) {
        PiaoExternalDTO result = piaoService.findPiaoExternal(codPAFK);
        return ResponseEntity.ok(result);
    }

    /**
     * Endpoint per recuperare i dati external di più PIAO a partire da una lista di ID.
     * Usato dalla generazione Excel batch.
     */
    @PostMapping("/external/byIds")
    public ResponseEntity<List<PiaoExternalDTO>> findPiaoExternalByIds(@RequestBody List<Long> idPiaoList) {
        List<PiaoExternalDTO> result = piaoService.findPiaoExternalByIds(idPiaoList);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/validazione/{idPiao}")
    public ResponseEntity<PiaoDTO> richiediValidazione(@PathVariable Long idPiao,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode) {
        return ResponseEntity.ok(piaoService.richiediValidazione(idPiao, userNameSurname, userRole, fiscalCode));
    }

    @PatchMapping("/valida-sezione/{idPiao}")
    public ResponseEntity<PiaoDTO> validaSezione(@PathVariable Long idPiao,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode) {
        return ResponseEntity.ok(piaoService.validaSezione(idPiao, userNameSurname, userRole, fiscalCode));
    }

    @PatchMapping("/rifiuta-validazione/{idPiao}")
    public ResponseEntity<PiaoDTO> rifiutaValidazione(@PathVariable Long idPiao, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode) {
        return ResponseEntity.ok(piaoService.rifiutaValidazione(idPiao, osservazioni, userNameSurname, userRole, fiscalCode));
    }

    @PatchMapping("/revoca-validazione/{idPiao}")
    public ResponseEntity<PiaoDTO> revocaValidazione(@PathVariable Long idPiao, @RequestBody String osservazioni,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode) {
        return ResponseEntity.ok(piaoService.revocaValidazione(idPiao, osservazioni, userNameSurname, userRole, fiscalCode));
    }

    @PatchMapping("/annulla-validazione/{idPiao}")
    public ResponseEntity<PiaoDTO> annullaValidazione(@PathVariable Long idPiao,
            @RequestHeader(value = "X-Updated-By-Name-Surname", required = false) String userNameSurname,
            @RequestHeader(value = "X-Updated-By-Role", required = false) String userRole,
            @RequestHeader(value = "X-Fiscal-Code", required = false) String fiscalCode) {
        return ResponseEntity.ok(piaoService.annullaValidazione(idPiao, userNameSurname, userRole, fiscalCode));
    }

    @GetMapping("/external/findAllPiaoPubblicati")
    public ResponseEntity<List<DocumentoPiaoExternalPPDTO>> findAllPiaoPubblicati(@RequestParam(required = false) Long idPiao,
                                                                                  @RequestParam(required = false) String denominazione,
                                                                                  @RequestParam(required = false) String codePa) {
        return ResponseEntity.ok(piaoService.findAllPiaoPubblicati(idPiao, denominazione, codePa));
    }


    @GetMapping("/trienni-riferimento")
    public ResponseEntity<List<String>> getTrienniRiferimento() {
        return ResponseEntity.ok(piaoService.getTrienniRiferimento());
    }

    @GetMapping("/pubblicati")
    public ResponseEntity<List<PiaoDTO>> findAllPiaoPubblicatiByCodePA(
            @RequestParam(required = false) String codPAFK) {
        return ResponseEntity.ok(piaoService.findAllPiaoPubblicatiByCodePA(codPAFK));
    }

    @GetMapping("/pubblicati/search")
    public ResponseEntity<List<PiaoDTO>> searchPubblicati(
            @RequestParam(name = "codiceIpa", required = false) String codiceIpa,
            @RequestParam(name = "tipologia", required = false) String tipologia) {
        return ResponseEntity.ok(piaoService.searchPubblicati(codiceIpa, tipologia));
    }

    @GetMapping("/pubblicati/search-by-denominazione")
    public ResponseEntity<List<PiaoDTO>> searchPubblicatiByDenominazione(
            @RequestParam(name = "denominazione") String denominazione,
            @RequestParam(name = "tipologia", required = false) String tipologia) {
        return ResponseEntity.ok(piaoService.searchPubblicatiByDenominazione(denominazione, tipologia));
    }

    @PutMapping("/salva-bozza-pdf")
    public ResponseEntity<Void> salvaInBozzaPiaoPDF(@RequestBody PiaoDTO piao)
    {
        piaoService.salvaInBozzaPiaoPDF(piao);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/pubblica-pdf")
    public ResponseEntity<Void> pubblicaPiaoPDF(@RequestBody PiaoDTO piao)
    {
        piaoService.pubblicaPiaoPDF(piao);
        return ResponseEntity.noContent().build();
    }

}
