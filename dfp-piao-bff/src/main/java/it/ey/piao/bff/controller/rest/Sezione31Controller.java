package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.GraficoSezione31DTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione31DTO;
import it.ey.piao.bff.service.ISezione31Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/sezione31")
public class Sezione31Controller
{
    private final ISezione31Service sezione31Service;

    public Sezione31Controller(ISezione31Service sezione31Service) {
        this.sezione31Service = sezione31Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody Sezione31DTO request)
    {
        return sezione31Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id,
                                                                                 @RequestParam String codicePa,
                                                                                 @RequestParam(required = false) String testoSezione,
                                                                                 @RequestParam(required = false) String campiModificati)
    {
        return sezione31Service.richiediValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @PostMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione31DTO>>> getOrCreatePIAO(@RequestBody PiaoDTO request)
    {
        return sezione31Service.getOrCreate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione31DTO>>> getByIdPiao(@PathVariable Long idPiao)
    {
        return sezione31Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }


    @PatchMapping("/valida-sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long id,
                                                                        @RequestParam String codicePa,
                                                                        @RequestParam(required = false) String testoSezione,
                                                                        @RequestParam(required = false) String campiModificati) {
        return sezione31Service.validaSezione(id, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/rifiuta-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione31Service.rifiutaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/revoca-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                            @RequestParam String codicePa,
                                                                            @RequestParam(required = false) String testoSezione,
                                                                            @RequestParam(required = false) String campiModificati) {
        return sezione31Service.revocaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long id,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione31Service.annullaValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @GetMapping("/grafico")
    public Mono<ResponseEntity<GenericResponseDTO<List<GraficoSezione31DTO>>>> getGraficoSezione4Mock() {
        return sezione31Service.getGraficoSezione31Mock().map(ResponseEntity::ok);
    }
}
