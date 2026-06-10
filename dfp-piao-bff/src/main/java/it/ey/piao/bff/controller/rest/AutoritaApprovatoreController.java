package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AutoritaApprovatoreDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.piao.bff.service.IAutoritaApprovatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/autorita-approvatore")
public class AutoritaApprovatoreController {
    private final IAutoritaApprovatoreService autoritaApprovatoreService;

    public AutoritaApprovatoreController(IAutoritaApprovatoreService autoritaApprovatoreService) {
        this.autoritaApprovatoreService = autoritaApprovatoreService;
    }

    @GetMapping
    public Mono<ResponseEntity<GenericResponseDTO<List<AutoritaApprovatoreDTO>>>> getAll()
    {
        return autoritaApprovatoreService.getAll()
            .map(ResponseEntity::ok);
    }
}
