package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione331DTO;
import it.ey.piao.bff.service.ISezione331Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@ApiV1Controller("/sezione331")
public class Sezione331Controller {

    private  final ISezione331Service sezione331Service;

    public Sezione331Controller(ISezione331Service sezione331Service) {
        this.sezione331Service = sezione331Service;
    }


    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione331DTO>>> save(@RequestBody Sezione331DTO request) {
        return sezione331Service.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @PatchMapping("/validazione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id) {
        return sezione331Service.richiediValidazione(id).map(ResponseEntity::ok);
    }

    @PostMapping("/piao")
    public Mono<ResponseEntity<GenericResponseDTO<Sezione331DTO>>> getOrCreatePIAO(@RequestBody PiaoDTO request) {
        return sezione331Service.getOrCreate(request)
            .map(ResponseEntity::ok);
    }
}
