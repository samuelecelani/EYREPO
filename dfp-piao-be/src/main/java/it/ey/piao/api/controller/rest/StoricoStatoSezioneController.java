package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StoricoStatoSezioneDTO;
import it.ey.piao.api.service.IStoricoStatoSezioneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@ApiV1Controller("/storico-stato-sezione")
@RequiredArgsConstructor
public class StoricoStatoSezioneController {

    private final IStoricoStatoSezioneService storicoStatoSezioneService;

    @PostMapping("/save")
    public ResponseEntity<StoricoStatoSezioneDTO> save(@RequestBody StoricoStatoSezioneDTO dto) {
        return ResponseEntity.ok(storicoStatoSezioneService.save(dto));
    }
}
