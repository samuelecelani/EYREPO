package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StakeHolderDTO;
import it.ey.piao.api.service.IStakeholderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@ApiV1Controller("/stakeholder")
public class StakeholderController {

    private final IStakeholderService stakeholderService;

    public StakeholderController(IStakeholderService stakeholderService) {
        this.stakeholderService = stakeholderService;
    }

    @GetMapping("/piao/{idPiao}")
    public ResponseEntity<List<StakeHolderDTO>> findBySezione1(@PathVariable Long idPiao) {
        return ResponseEntity.ok(stakeholderService.findByidPiao(idPiao));
    }

    @PostMapping("/save")
    public ResponseEntity<StakeHolderDTO> save(@RequestBody StakeHolderDTO request) {
        return ResponseEntity.ok(stakeholderService.save(request));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStakeholder(@PathVariable Long id) {
        stakeholderService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

