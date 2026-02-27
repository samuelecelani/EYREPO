package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione21DTO;
import it.ey.dto.Sezione22DTO;
import it.ey.piao.bff.service.ISezione22Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione22")
public class Sezione22Controller {

    private final ISezione22Service sezione22Service;

    public Sezione22Controller(ISezione22Service sezione22Service) {
        this.sezione22Service = sezione22Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody Sezione22DTO request) {
        return sezione22Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id) {
        return sezione22Service.richiediValidazione(id).map(ResponseEntity::ok);
    }

    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione22DTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return sezione22Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }
}


