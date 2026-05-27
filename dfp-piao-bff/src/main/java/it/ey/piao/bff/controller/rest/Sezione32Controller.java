package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione32DTO;
import it.ey.piao.bff.service.ISezione32Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione32")
public class Sezione32Controller
{
    private final ISezione32Service sezione32Service;

    public Sezione32Controller(ISezione32Service sezione32Service) {
        this.sezione32Service = sezione32Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody Sezione32DTO request) {
        return sezione32Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id,
                                                                                 @RequestParam String codicePa,
                                                                                 @RequestParam(required = false) String testoSezione,
                                                                                 @RequestParam(required = false) String campiModificati) {
        return sezione32Service.richiediValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @PostMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione32DTO>>> getOrCreatePIAO(@RequestBody PiaoDTO request) {
        return sezione32Service.getOrCreate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione32DTO>>> getByIdPiao(@PathVariable Long idPiao)
    {
        return sezione32Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }


    @PatchMapping("/valida-sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long id,
                                                                        @RequestParam String codicePa,
                                                                        @RequestParam(required = false) String testoSezione,
                                                                        @RequestParam(required = false) String campiModificati)
    {
        return sezione32Service.validaSezione(id, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/rifiuta-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati)
    {
        return sezione32Service.rifiutaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/revoca-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long id, @RequestBody String osservazioni,
                                                                            @RequestParam String codicePa,
                                                                            @RequestParam(required = false) String testoSezione,
                                                                            @RequestParam(required = false) String campiModificati)
    {
        return sezione32Service.revocaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long id,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati)
    {
        return sezione32Service.annullaValidazione(id, codicePa).map(ResponseEntity::ok);
    }
}
