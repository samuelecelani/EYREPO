package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StakeHolderDTO;
import it.ey.piao.bff.service.IStakeholderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/stakeholder")
public class StakeholderController {

    private final IStakeholderService stakeholderService;

    public StakeholderController(IStakeholderService stakeholderService) {
        this.stakeholderService = stakeholderService;
    }

    @GetMapping("/piao/{idPiao}")
    public Mono<ResponseEntity<GenericResponseDTO<List<StakeHolderDTO>>>> findByPiao(@PathVariable Long idPiao) {
        return stakeholderService.findByidPiao(idPiao)
            .map(ResponseEntity::ok);
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<StakeHolderDTO>>> save(@RequestBody StakeHolderDTO request) {
        return stakeholderService.save(request)
            .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteById(@PathVariable Long id) {
        return stakeholderService.deleteById(id)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }
}

