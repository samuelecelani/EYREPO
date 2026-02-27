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
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id) {
        return sezione4Service.richiediValidazione(id).map(ResponseEntity::ok);
    }
}
