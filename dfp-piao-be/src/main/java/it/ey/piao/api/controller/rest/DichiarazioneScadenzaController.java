package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.PageDTO;
import it.ey.dto.SollecitiDichiarazioniDFPDTO;
import it.ey.dto.StoricoDichiarazioneDFPDTO;
import it.ey.enums.StatoDichiarazioneEnum;
import it.ey.piao.api.service.IDichiarazioneScadenzaService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/dichiarazione-scadenza")
public class DichiarazioneScadenzaController
{
    private final IDichiarazioneScadenzaService dichiarazioneScadenzaService;

    public DichiarazioneScadenzaController(IDichiarazioneScadenzaService dichiarazioneScadenzaService) {
        this.dichiarazioneScadenzaService = dichiarazioneScadenzaService;
    }

    @PostMapping
    public ResponseEntity<DichiarazioneScadenzaDTO> save(@RequestBody DichiarazioneScadenzaDTO dichiarazioneScadenzaDTO) {
        return ResponseEntity.ok(dichiarazioneScadenzaService.saveOrUpdate(dichiarazioneScadenzaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        dichiarazioneScadenzaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{codPAFK}")
    public ResponseEntity<DichiarazioneScadenzaDTO> getExistingDichiarazioneScadenza(@PathVariable String codPAFK)
    {
        return ResponseEntity.ok(dichiarazioneScadenzaService.getExistingDichiarazioneScadenza(codPAFK));
    }

    /** Recupera la DichiarazioneScadenza collegata al PIAO con id={idPiao} (può essere null). */
    @GetMapping("/by-piao/{idPiao}")
    public ResponseEntity<DichiarazioneScadenzaDTO> findByIdPiao(@PathVariable Long idPiao)
    {
        return ResponseEntity.ok(dichiarazioneScadenzaService.findByIdPiao(idPiao));
    }

    @GetMapping("/all")
    public ResponseEntity<List<StoricoDichiarazioneDFPDTO>> findAllStorico() {
        return ResponseEntity.ok(dichiarazioneScadenzaService.findAllStorico());
    }

    @PatchMapping("/{id}/stato")
    public ResponseEntity<Void> updateStato(@PathVariable Long id,
                                            @RequestBody Boolean stato) {
        dichiarazioneScadenzaService.updateStato(id, stato);
        return ResponseEntity.noContent().build();
    }

    /**
     * Ricerca le dichiarazioni di mancata/ritardata compilazione PIAO con filtri:
     * <ul>
     *     <li><b>denominazionePiao</b> (obbligatorio): denominazione del PIAO (LIKE)</li>
     *     <li><b>tipologiaIstat</b> (opzionale): filtro tipologia di Amministrazioni</li>
     *     <li><b>codPAFK</b> (opzionale): filtro su una specifica Amministrazione</li>
     *     <li><b>statoDichiarazione</b> (opzionale): INVIATA / NON_INVIATA</li>
     * </ul>
     */
    @GetMapping("/search")
    public ResponseEntity<List<SollecitiDichiarazioniDFPDTO>> searchDichiarazioni(
        @RequestParam("denominazionePiao") String denominazionePiao,
        @RequestParam(value = "tipologiaIstat", required = false) String tipologiaIstat,
        @RequestParam(value = "codPAFK", required = false) String codPAFK,
        @RequestParam(value = "statoDichiarazione", required = false) StatoDichiarazioneEnum statoDichiarazione)
    {
        return ResponseEntity.ok(
            dichiarazioneScadenzaService.searchDichiarazioni(denominazionePiao, tipologiaIstat, codPAFK, statoDichiarazione));
    }

    /**
     * Variante PAGINATA del search.
     * Spring inietta automaticamente {@link Pageable} dai query params {@code page}, {@code size}, {@code sort}.
     * Esempio: GET /search/paged?denominazionePiao=PIAO&page=0&size=20&sort=amministrazione,asc
     */
    @GetMapping("/search/paged")
    public ResponseEntity<PageDTO<SollecitiDichiarazioniDFPDTO>> searchDichiarazioniPaged(
        @RequestParam("denominazionePiao") String denominazionePiao,
        @RequestParam(value = "tipologiaIstat", required = false) String tipologiaIstat,
        @RequestParam(value = "codPAFK", required = false) String codPAFK,
        @RequestParam(value = "statoDichiarazione", required = false) StatoDichiarazioneEnum statoDichiarazione,
        Pageable pageable)
    {
        return ResponseEntity.ok(PageDTO.from(
            dichiarazioneScadenzaService.searchDichiarazioniPaged(
                denominazionePiao, tipologiaIstat, codPAFK, statoDichiarazione, pageable)));
    }
}
