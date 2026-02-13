package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.piao.bff.service.IMonitoraggioPrevenzioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

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
    public Mono<ResponseEntity<GenericResponseDTO<MonitoraggioPrevenzioneDTO>>> save(@RequestBody MonitoraggioPrevenzioneDTO request)
    {
        return monitoraggioPrevenzioneService.saveOrUpdate(request)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/misura-prevenzione-evento-rischio/{idMisuraPrevenzioneEventoRischio}")
    public Mono<ResponseEntity<GenericResponseDTO<List<MonitoraggioPrevenzioneDTO>>>> getAllByMisuraPrevenzioneEventoRischio(@PathVariable Long idMisuraPrevenzioneEventoRischio)
    {
        return monitoraggioPrevenzioneService.getAllByMisuraPrevenzioneEventoRischio(idMisuraPrevenzioneEventoRischio)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id)
    {
        return monitoraggioPrevenzioneService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
