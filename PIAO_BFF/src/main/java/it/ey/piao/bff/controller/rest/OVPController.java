package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.ItemMatriceDTO;
import it.ey.dto.OVPDTO;
import it.ey.piao.bff.service.IOVPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@ApiV1Controller("/ovp")
public class OVPController {

    private final IOVPService ovpService;

    public OVPController(IOVPService ovpService) {
        this.ovpService = ovpService;
    }


    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<OVPDTO>>> save(@RequestBody OVPDTO request) {
        return ovpService.saveOrUpdate(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/sezione/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<List<OVPDTO>>>> richiestaDiValidazione(@PathVariable Long id) {
        return ovpService.getOvpByIdSezione21(id).map(ResponseEntity::ok);
    }

    @GetMapping("/piao/{piaoId}")
    public Mono<ResponseEntity<GenericResponseDTO<List<OVPDTO>>>> getAllByPiao(@PathVariable Long piaoId) {
        return ovpService.getOvpByPiaoId(piaoId).map(ResponseEntity::ok);
    }

    @GetMapping("/matrice")
    public Mono<ResponseEntity<GenericResponseDTO<List<ItemMatriceDTO>>>> getMatrice(
            @RequestParam(required = false) Long idSezione21,
            @RequestParam Long idSezione1,
            @RequestParam(required = false) Long idPiao) {
        return ovpService.getOvpMatriceByIdSezione21(idSezione21, idSezione1, idPiao).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(@PathVariable Long id) {
        return ovpService.delete(id).map(ResponseEntity::ok);
    }
}
