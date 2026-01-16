package it.ey.piao.bff.controller.rest;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.OVPDTO;
import it.ey.piao.bff.service.IOVPService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/ovp")
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

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<GenericResponseDTO<Void>>> delete(@PathVariable Long id) {
        return ovpService.delete(id).map(ResponseEntity::ok);
    }
}
