package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AnagraficaDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAnagraficaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/anagrafica")
public class AnagraficaController {

    private final IAnagraficaService anagraficaService;

    public AnagraficaController(IAnagraficaService anagraficaService) {
        this.anagraficaService = anagraficaService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<AnagraficaDTO>>>> getAll() {
        return anagraficaService.getAll().map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<AnagraficaDTO>>> save(@RequestBody AnagraficaDTO anagraficaDTO) {
        return anagraficaService.save(anagraficaDTO).map(ResponseEntity::ok);
    }
}



