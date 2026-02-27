package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IDynamicTableIteroperabilitaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.Map;

@ApiV1Controller("/tabella-iteroperabilita")
public class DynamicTableIteroperabilitaController
{
    private final IDynamicTableIteroperabilitaService dynamicTableIteroperabilitaService;

    public DynamicTableIteroperabilitaController(IDynamicTableIteroperabilitaService dynamicTableIteroperabilitaService)
    {
        this.dynamicTableIteroperabilitaService = dynamicTableIteroperabilitaService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<Map<String, Object>>>> getTable()
    {
        return dynamicTableIteroperabilitaService.buildDynamicTableIteroperabilitaMock()
            .map(ResponseEntity::ok);
    }
}
