package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PageDTO;
import it.ey.dto.SollecitiDichiarazioniDFPDTO;
import it.ey.dto.StoricoDichiarazioneDFPDTO;
import it.ey.enums.StatoDichiarazioneEnum;
import it.ey.piao.bff.service.IDichiarazioneScadenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/dichiarazione-scadenza")
public class DichiarazioneScadenzaController
{
    private final IDichiarazioneScadenzaService dichiarazioneScadenzaService;

    public DichiarazioneScadenzaController(IDichiarazioneScadenzaService dichiarazioneScadenzaService)
    {
        this.dichiarazioneScadenzaService = dichiarazioneScadenzaService;
    }

    @PostMapping
    Mono<ResponseEntity<GenericResponseDTO<DichiarazioneScadenzaDTO>>> save(@RequestBody DichiarazioneScadenzaDTO dto)
    {
        return dichiarazioneScadenzaService.saveOrUpdate(dto)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id)
    {
        return dichiarazioneScadenzaService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().build()));
    }

    @GetMapping("/{codPAFK}")
    Mono<ResponseEntity<GenericResponseDTO<DichiarazioneScadenzaDTO>>> getExistingDichiarazioneScadenza(@PathVariable String codPAFK) {
        return dichiarazioneScadenzaService.getExistingDichiarazioneScadenza(codPAFK)
            .map(ResponseEntity::ok);
    }

    /** Recupera la DichiarazioneScadenza collegata al PIAO con id={idPiao} (può essere null). */
    @GetMapping("/by-piao/{idPiao}")
    Mono<ResponseEntity<GenericResponseDTO<DichiarazioneScadenzaDTO>>> findByIdPiao(@PathVariable Long idPiao) {
        return dichiarazioneScadenzaService.findByIdPiao(idPiao)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/all")
    Mono<ResponseEntity<GenericResponseDTO<List<StoricoDichiarazioneDFPDTO>>>> findAllStorico() {
        return dichiarazioneScadenzaService.findAllStorico()
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/stato")
    Mono<ResponseEntity<GenericResponseDTO<Void>>> updateStato(@PathVariable Long id,
                                                               @RequestBody Boolean stato) {
        return dichiarazioneScadenzaService.updateStato(id, stato)
            .map(ResponseEntity::ok);
    }

    /**
     * Ricerca dichiarazioni di mancata/ritardata compilazione PIAO con filtri opzionali.
     * <ul>
     *     <li><b>denominazionePiao</b>: obbligatorio (denominazione del PIAO, LIKE)</li>
     *     <li><b>tipologiaIstat</b>: opzionale (filtro tipologia di Amministrazioni)</li>
     *     <li><b>codPAFK</b>: opzionale (filtro su singola Amministrazione)</li>
     *     <li><b>statoDichiarazione</b>: opzionale (INVIATA / NON_INVIATA)</li>
     * </ul>
     */
    @GetMapping("/search")
    Mono<ResponseEntity<GenericResponseDTO<List<SollecitiDichiarazioniDFPDTO>>>> searchDichiarazioni(
        @RequestParam("denominazionePiao") String denominazionePiao,
        @RequestParam(value = "tipologiaIstat", required = false) String tipologiaIstat,
        @RequestParam(value = "codPAFK", required = false) String codPAFK,
        @RequestParam(value = "statoDichiarazione", required = false) StatoDichiarazioneEnum statoDichiarazione)
    {
        return dichiarazioneScadenzaService
            .searchDichiarazioni(denominazionePiao, tipologiaIstat, codPAFK, statoDichiarazione)
            .map(ResponseEntity::ok);
    }

    /**
     * Variante PAGINATA del search.
     * Esempio: GET /search/paged?denominazionePiao=PIAO&page=0&size=20&sort=amministrazione,asc
     * <p>
     * NOTA: il parametro {@code sort} viene letto direttamente dai query params dell'exchange
     * (non come {@code @RequestParam List<String>}) per evitare lo splitting automatico di Spring
     * sulla virgola, che spezzerebbe "statoDichiarazione,desc" in due elementi separati
     * causando la perdita della direction.
     */
    @GetMapping("/search/paged")
    Mono<ResponseEntity<GenericResponseDTO<PageDTO<SollecitiDichiarazioniDFPDTO>>>> searchDichiarazioniPaged(
        @RequestParam("denominazionePiao") String denominazionePiao,
        @RequestParam(value = "tipologiaIstat", required = false) String tipologiaIstat,
        @RequestParam(value = "codPAFK", required = false) String codPAFK,
        @RequestParam(value = "statoDichiarazione", required = false) StatoDichiarazioneEnum statoDichiarazione,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size,
        ServerWebExchange exchange)
    {
        // Leggo i sort raw (uno per ogni occorrenza di ?sort=...) senza splitting automatico sulla virgola
        List<String> sort = exchange.getRequest().getQueryParams().get("sort");
        return dichiarazioneScadenzaService
            .searchDichiarazioniPaged(denominazionePiao, tipologiaIstat, codPAFK, statoDichiarazione, page, size, sort)
            .map(ResponseEntity::ok);
    }
}
