package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.piao.api.service.IMonitoraggioPrevenzioneService;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<Void> deleteById(@PathVariable Long id,
                                           @RequestParam(required = false) String campiModificati,
                                           @RequestParam(required = false) Long idPiao,
                                           @RequestParam(required = false) String testoSezione,
                                           HttpServletRequest request)
    {
        String updatedByNameSurname = request.getHeader("X-Updated-By-Name-Surname");
        String updatedByRole = request.getHeader("X-Updated-By-Role");
        String statoSezione = request.getHeader("X-Stato-Sezione");
        monitoraggioPrevenzioneService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
        return ResponseEntity.noContent().build();
    }
}
