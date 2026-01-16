package it.ey.piao.bff.controller.rest;

import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IDynamicTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/tabella")
public class DynamicTableController {
private final IDynamicTableService dynamicTableService;

    public DynamicTableController(IDynamicTableService dynamicTableService) {
        this.dynamicTableService = dynamicTableService;
    }
    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<Map<String, Object>>>> getTable() {
        return dynamicTableService.buildDynamicTableMock()
            .map(ResponseEntity::ok);
    }
}
