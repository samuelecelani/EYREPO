package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AnagraficaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.Sezione1DTO;
import it.ey.piao.bff.service.ISezione1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione1")
public class Sezione1Controller {

    private final ISezione1Service sezione1Service;


    public Sezione1Controller(ISezione1Service sezione1Service) {
        this.sezione1Service = sezione1Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody Sezione1DTO request) {
        return sezione1Service.saveOrUpdate(request).map(ResponseEntity::ok);
    }
        @GetMapping("/{idPiao}")
        public Mono<ResponseEntity<GenericResponseDTO<Sezione1DTO>>> getByIdPiao(@PathVariable Long idPiao,@RequestParam(required = false) String codiceFiscale) {
            return sezione1Service.findByPiao(idPiao,codiceFiscale).map(ResponseEntity::ok);
        }
    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id,
                                                                                 @RequestParam String codicePa,
                                                                                 @RequestParam(required = false) String testoSezione,
                                                                                 @RequestParam(required = false) String campiModificati) {
        return sezione1Service.richiediValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/valida-sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> validaSezione(@PathVariable Long id,
                                                                        @RequestParam String codicePa,
                                                                        @RequestParam(required = false) String testoSezione,
                                                                        @RequestParam(required = false) String campiModificati) {
        return sezione1Service.validaSezione(id, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/rifiuta-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> rifiutaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione1Service.rifiutaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }
    @PatchMapping("/revoca-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> revocaValidazione(@PathVariable Long id,@RequestBody String osservazioni,
                                                                            @RequestParam String codicePa,
                                                                            @RequestParam(required = false) String testoSezione,
                                                                            @RequestParam(required = false) String campiModificati) {
        return sezione1Service.revocaValidazione(id, osservazioni, codicePa).map(ResponseEntity::ok);
    }

    @PatchMapping("/annulla-validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> annullaValidazione(@PathVariable Long id,
                                                                             @RequestParam String codicePa,
                                                                             @RequestParam(required = false) String testoSezione,
                                                                             @RequestParam(required = false) String campiModificati) {
        return sezione1Service.annullaValidazione(id, codicePa).map(ResponseEntity::ok);
    }

    @GetMapping("/anagrafica-ipa")
    public Mono<ResponseEntity<GenericResponseDTO<AnagraficaDTO>>> getAnagraficaFromIpa(@RequestParam String codiceFiscale) {
        return sezione1Service.getAnagraficaFromIpa(codiceFiscale).map(ResponseEntity::ok);
    }

}
