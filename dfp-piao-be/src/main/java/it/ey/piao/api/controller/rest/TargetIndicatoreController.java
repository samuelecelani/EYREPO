package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.TargetIndicatoreDTO;
import it.ey.piao.api.service.ITargetIndicatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@ApiV1Controller("/target-indicatore")
public class TargetIndicatoreController {

    private final ITargetIndicatoreService targetIndicatoreService;

    public TargetIndicatoreController(ITargetIndicatoreService targetIndicatoreService) {
        this.targetIndicatoreService = targetIndicatoreService;
    }

    @GetMapping
    public ResponseEntity<List<TargetIndicatoreDTO>> getAllTargetIndicatore() {
        return ResponseEntity.ok(targetIndicatoreService.findAll());
    }
}
