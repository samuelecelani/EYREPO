package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.piao.api.service.IMonitoraggioPrevenzioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@ApiV1Controller("/monitoraggio-prevenzione")
public class MonitoraggioPrevenzioneController
{
    private final IMonitoraggioPrevenzioneService monitoraggioPrevenzioneService;

    public MonitoraggioPrevenzioneController(IMonitoraggioPrevenzioneService monitoraggioPrevenzioneService)
    {
        this.monitoraggioPrevenzioneService = monitoraggioPrevenzioneService;
    }

    @PostMapping("/save")
    public ResponseEntity<MonitoraggioPrevenzioneDTO> saveOrUpdate(@RequestBody MonitoraggioPrevenzioneDTO request) {
        MonitoraggioPrevenzioneDTO response = monitoraggioPrevenzioneService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Recupera tutti i MonitoraggioPrevenzione per una MisuraPrevenzioneEventoRischio.
     */
    @GetMapping("/misura-prevenzione-evento-rischio/{idMisuraPrevenzioneEventoRischio}")
    public ResponseEntity<List<MonitoraggioPrevenzioneDTO>> getAllByMisuraPrevenzioneEventoRischio(@PathVariable Long idMisuraPrevenzioneEventoRischio) {
        List<MonitoraggioPrevenzioneDTO> response = monitoraggioPrevenzioneService.getAllByMisuraPrevenzioneEventoRischio(idMisuraPrevenzioneEventoRischio);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un MonitoraggioPrevenzione per ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        monitoraggioPrevenzioneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
