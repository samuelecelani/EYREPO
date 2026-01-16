package it.ey.piao.bff.controller.rest;

import it.ey.dto.ConfigFeInitializerDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IConfigInitializerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/config/initializer")
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
