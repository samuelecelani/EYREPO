package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.TargetIndicatoreDTO;
import it.ey.piao.bff.service.ITargetIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/target-indicatore")
public class TargetIndicatoreController {

    private final ITargetIndicatoreService targetIndicatoreService;

    public TargetIndicatoreController(ITargetIndicatoreService targetIndicatoreService) {
        this.targetIndicatoreService = targetIndicatoreService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<TargetIndicatoreDTO>>>> getTargetIndicatore() {
        return targetIndicatoreService.getTargetIndicatore()
            .map(ResponseEntity::ok);
    }
}

