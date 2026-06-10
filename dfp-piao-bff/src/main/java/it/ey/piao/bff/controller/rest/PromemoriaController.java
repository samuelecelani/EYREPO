package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.PromemoriaDTO;
import it.ey.piao.bff.service.IPromemoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/promemoria")
public class PromemoriaController
{
    private final IPromemoriaService promemoriaService;

    public PromemoriaController(IPromemoriaService promemoriaService)
    {
        this.promemoriaService = promemoriaService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<PromemoriaDTO>>>> getAll()
    {
        return promemoriaService.getAll()
            .map(ResponseEntity::ok);
    }
}
