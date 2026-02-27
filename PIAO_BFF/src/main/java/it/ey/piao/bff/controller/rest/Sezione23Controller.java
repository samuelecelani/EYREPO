package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione22DTO;
import it.ey.dto.Sezione23DTO;
import it.ey.piao.bff.service.ISezione23Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione23")
public class Sezione23Controller {

    private final ISezione23Service sezione23Service;

    public Sezione23Controller(ISezione23Service sezione23Service) {
        this.sezione23Service = sezione23Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody Sezione23DTO request) {
        return sezione23Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id) {
        return sezione23Service.richiediValidazione(id).map(ResponseEntity::ok);
    }

    @PostMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione23DTO>>> getOrCreatePIAO(@RequestBody PiaoDTO request) {
        return sezione23Service.getOrCreate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione23DTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return sezione23Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }
}


