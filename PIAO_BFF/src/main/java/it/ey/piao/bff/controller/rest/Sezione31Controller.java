package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PiaoDTO;
import it.ey.dto.Sezione31DTO;
import it.ey.piao.bff.service.ISezione31Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> richiestaDiValidazione(@PathVariable Long id)
    {
        return sezione31Service.richiediValidazione(id).map(ResponseEntity::ok);
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
}
