package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.PiaoDTO;
import it.ey.piao.api.service.IPiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

@ApiV1Controller("/piao")
@RequiredArgsConstructor
public class PiaoController {

    private final IPiaoService piaoService;

    @PostMapping("/getOrCreate")
    public ResponseEntity<PiaoDTO> getOrCreate(@RequestBody PiaoDTO dto) {
        PiaoDTO result = piaoService.getOrCreatePiao( dto);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/redigiPiaoIsAllowed")
    public ResponseEntity<Boolean> getOrCreate(@RequestParam String codPAFK) {
        return ResponseEntity.ok(piaoService.redigiPiaoIsAllowed( codPAFK));
    }
    @GetMapping("/findBycodPAFK")
    public ResponseEntity<List<PiaoDTO>> findByCodPAFK(@RequestParam String codPAFK) {
        return ResponseEntity.ok(piaoService.getAllPiaoByCodPAFK( codPAFK));
    }
}
