package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.*;
import it.ey.piao.bff.service.ISezione332Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/sezione332")
public class Sezione332Controller
{
    private  final ISezione332Service sezione332Service;

    public Sezione332Controller(ISezione332Service sezione332Service) {
        this.sezione332Service = sezione332Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione332DTO>>> save(@RequestBody Sezione332DTO request)
    {
        return sezione332Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione332DTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return sezione332Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id,
                                                                                 @RequestParam String codicePa,
                                                                                 @RequestParam(required = false) String testoSezione,
                                                                                 @RequestParam(required = false) String campiModificati)
    {
        return sezione332Service.richiediValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @PostMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione332DTO>>> getOrCreatePIAO(@RequestBody PiaoDTO request)
    {
        return sezione332Service.getOrCreate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/valida-sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long id,
                                                                        @RequestParam String codicePa,
                                                                        @RequestParam(required = false) String testoSezione,
                                                                        @RequestParam(required = false) String campiModificati)
    {
        return sezione332Service.validaSezione(id, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/rifiuta-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati)
    {
        return sezione332Service.rifiutaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/revoca-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                            @RequestParam String codicePa,
                                                                            @RequestParam(required = false) String testoSezione,
                                                                            @RequestParam(required = false) String campiModificati)
    {
        return sezione332Service.revocaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long id,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati)
    {
        return sezione332Service.annullaValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @GetMapping("/tipologia-attivita")
    public Mono<ResponseEntity<GenericResponseDTO<List<TipologiaAttivitaDTO>>>> getTipologiaAttivita()
    {
        return sezione332Service.getTipologiaAttivita()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/ambito-competenza")
    public Mono<ResponseEntity<GenericResponseDTO<List<AmbitoCompetenzaDTO>>>> getAmbitoCompetenza()
    {
        return sezione332Service.getAmbitoCompetenza()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/area-tematica")
    public Mono<ResponseEntity<GenericResponseDTO<List<AreaTematicaDTO>>>> getAreaTematica()
    {
        return sezione332Service.getAreaTematica()
            .map(ResponseEntity::ok);
    }

    @GetMapping("/tipologia-destinatari")
    public Mono<ResponseEntity<GenericResponseDTO<List<TipologiaDestinatariDTO>>>> getTipologiaDestinatari()
    {
        return sezione332Service.getTipologiaDestinatari()
            .map(ResponseEntity::ok);
    }
}
