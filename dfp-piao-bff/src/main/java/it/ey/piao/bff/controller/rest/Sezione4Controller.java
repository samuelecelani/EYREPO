package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Sezione4DTO;
import it.ey.piao.bff.service.ISezione4Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione4")
public class Sezione4Controller {

    private final ISezione4Service sezione4Service;

    public Sezione4Controller(ISezione4Service sezione4Service) {
        this.sezione4Service = sezione4Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione4DTO>>> save(@RequestBody Sezione4DTO request) {
        return sezione4Service.saveOrUpdate(request)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id,
                                                                                 @RequestParam String codicePa,
                                                                                 @RequestParam(required = false) String testoSezione,
                                                                                 @RequestParam(required = false) String campiModificati) {
        return sezione4Service.richiediValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/valida-sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long id,
                                                                        @RequestParam String codicePa,
                                                                        @RequestParam(required = false) String testoSezione,
                                                                        @RequestParam(required = false) String campiModificati) {
        return sezione4Service.validaSezione(id, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/rifiuta-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione4Service.rifiutaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/revoca-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                            @RequestParam String codicePa,
                                                                            @RequestParam(required = false) String testoSezione,
                                                                            @RequestParam(required = false) String campiModificati) {
        return sezione4Service.revocaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long id,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione4Service.annullaValidazione(id, codicePa).map(ResponseEntity::ok);

    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione4DTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return sezione4Service.findByIdPiao(idPiao).map(ResponseEntity::ok);
    }
}
