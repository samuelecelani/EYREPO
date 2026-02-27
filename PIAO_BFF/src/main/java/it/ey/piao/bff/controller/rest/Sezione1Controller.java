package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
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
        public Mono<ResponseEntity<GenericResponseDTO<Sezione1DTO>>> getByIdPiao(@PathVariable Long idPiao) {
            return sezione1Service.findByPiao(idPiao).map(ResponseEntity::ok);
        }
    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id) {
        return sezione1Service.richiediValidazione(id).map(ResponseEntity::ok);
    }

}
