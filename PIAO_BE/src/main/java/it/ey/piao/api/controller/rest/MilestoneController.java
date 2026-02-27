package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;
import it.ey.piao.api.service.IMilestoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/milestone")
public class MilestoneController
{
    private final IMilestoneService milestoneService;

    public MilestoneController(IMilestoneService milestoneService)
    {
        this.milestoneService = milestoneService;
    }

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<MilestoneDTO> saveOrUpdate(@RequestBody MilestoneDTO request)
    {
        MilestoneDTO response = milestoneService.saveOrUpdate(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id)
    {
        milestoneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promemoria/{idMilestone}")
    public ResponseEntity<PromemoriaDTO> getPromemoriaByMilestone(@PathVariable Long idMilestone)
    {
        PromemoriaDTO response = milestoneService.getPromemoriaByMilestone(idMilestone);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sottofase-monitoraggio/{idSottofaseMonitoraggio}")
    public ResponseEntity<List<MilestoneDTO>> getAllBySottofaseMonitoraggio(@PathVariable Long idSottofaseMonitoraggio)
    {
        List<MilestoneDTO> response = milestoneService.getMilestoneBySottofaseMonitoraggio(idSottofaseMonitoraggio);
        return ResponseEntity.ok(response);
    }


}
