package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.MilestoneDTO;
import it.ey.dto.PromemoriaDTO;
import it.ey.piao.api.service.IMilestoneService;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("/save")
    public ResponseEntity<Void> saveOrUpdate(@RequestBody MilestoneDTO request)
    {
        milestoneService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

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
        milestoneService.deleteById(id, campiModificati, idPiao, testoSezione, updatedByNameSurname, updatedByRole, statoSezione);
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
