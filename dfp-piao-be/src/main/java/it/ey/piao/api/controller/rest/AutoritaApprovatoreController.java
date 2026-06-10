package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.AutoritaApprovatoreDTO;
import it.ey.piao.api.service.IAutoritaApprovatoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@ApiV1Controller("/autorita-approvatore")
public class AutoritaApprovatoreController
{
    private final IAutoritaApprovatoreService autoritaApprovatoreService;

    public AutoritaApprovatoreController(IAutoritaApprovatoreService autoritaApprovatoreService)
    {
        this.autoritaApprovatoreService = autoritaApprovatoreService;
    }

    @GetMapping
    public ResponseEntity<List<AutoritaApprovatoreDTO>> getAll()
    {
        List<AutoritaApprovatoreDTO> response = autoritaApprovatoreService.getAll();
        return ResponseEntity.ok(response);
    }
}
