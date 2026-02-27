package it.ey.piao.api.controller.rest;


import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.SottofaseMonitoraggioDTO;
import it.ey.piao.api.service.ISottofaseMonitoraggioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/sottofase-monitoraggio")
public class SottofaseMonitoraggioController {

    private final ISottofaseMonitoraggioService sottofaseService;

    public SottofaseMonitoraggioController(ISottofaseMonitoraggioService sottofaseService) {
        this.sottofaseService = sottofaseService;
    }

    @PostMapping("/save")
    public ResponseEntity<SottofaseMonitoraggioDTO> saveOrUpdate(@RequestBody SottofaseMonitoraggioDTO dto) {
        SottofaseMonitoraggioDTO saved = sottofaseService.saveOrUpdate(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/sezione4/{idSezione4}")
    public ResponseEntity<List<SottofaseMonitoraggioDTO>> getAllBySezione4(@PathVariable Long idSezione4) {
        List<SottofaseMonitoraggioDTO> sottofasi = sottofaseService.getAllBySezione4(idSezione4);
        return ResponseEntity.ok(sottofasi);
    }






    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        sottofaseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
