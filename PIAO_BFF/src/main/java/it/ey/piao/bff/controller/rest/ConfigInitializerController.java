package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.ConfigFeInitializerDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IConfigInitializerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@ApiV1Controller("/config/initializer")
public class ConfigInitializerController {

    private  final IConfigInitializerService configInitializerService;

    public ConfigInitializerController(IConfigInitializerService configInitializerService) {
        this.configInitializerService = configInitializerService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<ConfigFeInitializerDTO>>> configInitializer() {
        return configInitializerService.configInitializer()
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
