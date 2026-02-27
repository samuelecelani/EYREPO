package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione1DTO;
import it.ey.dto.Sezione21DTO;
import it.ey.piao.bff.service.ISezione21Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione21")
public class Sezione21Controller {

    private final ISezione21Service sezione21Service;


    public Sezione21Controller(ISezione21Service sezione21Service) {
        this.sezione21Service = sezione21Service;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> save(@RequestBody Sezione21DTO request) {
        return sezione21Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id) {
        return sezione21Service.richiediValidazione(id).map(ResponseEntity::ok);
    }
    @GetMapping("/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione21DTO>>> getByIdPiao(@PathVariable Long idPiao) {
        return sezione21Service.findByPiao(idPiao).map(ResponseEntity::ok);
    }
}


