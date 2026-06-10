package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.EventoRischioDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;
import it.ey.piao.bff.service.IEventoRischioService;
import it.ey.piao.bff.service.IMilestoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/milestone")
public class MilestoneController
{
    private final IMilestoneService milestoneService;

    public MilestoneController(IMilestoneService milestoneService)
    {
        this.milestoneService = milestoneService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<Void>> save(@RequestBody MilestoneDTO request)
    {
        return milestoneService.saveOrUpdate(request)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @GetMapping("/promemoria/{idPromemoria}")
    public Mono<ResponseEntity<GenericResponseDTO<List<PromemoriaDTO>>>> getPromemoriaByMilestone(@PathVariable Long idPromemoria)
    {
        return milestoneService.getPromemoriaByMilestone(idPromemoria)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/sottofase-monitoraggio/{idSottofaseMonitoraggio}")
    public Mono<ResponseEntity<GenericResponseDTO<List<MilestoneDTO>>>> getMilestoneBySottofaseMonitoraggio(@PathVariable Long idSottofaseMonitoraggio)
    {
        return milestoneService.getMilestoneBySottofaseMonitoraggio(idSottofaseMonitoraggio)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id,
                                                 @RequestParam(required = false) String campiModificati,
                                                 @RequestParam(required = false) Long idPiao,
                                                 @RequestParam(required = false) String testoSezione,
                                                            @RequestParam(required = false) String statoSezione) {
        return milestoneService.deleteById(id, campiModificati, idPiao, testoSezione)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}
