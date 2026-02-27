package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PromemoriaDTO;
import it.ey.piao.api.service.IPromemoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

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
    public ResponseEntity<List<PromemoriaDTO>> getAll()
    {
        List<PromemoriaDTO> response = promemoriaService.getAll();
        return ResponseEntity.ok( response );
    }
}
