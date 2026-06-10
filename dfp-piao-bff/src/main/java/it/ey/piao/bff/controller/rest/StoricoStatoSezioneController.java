package it.ey.piao.bff.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.GenericResponseDTO;
import it.ey.dto.StoricoStatoSezioneDTO;
import it.ey.piao.bff.service.IStoricoStatoSezioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@ApiV1Controller("/storico-stato-sezione")
public class StoricoStatoSezioneController {

    private final IStoricoStatoSezioneService storicoStatoSezioneService;

    public StoricoStatoSezioneController(IStoricoStatoSezioneService storicoStatoSezioneService) {
        this.storicoStatoSezioneService = storicoStatoSezioneService;
    }

    @PostMapping("/save")
    public Mono<ResponseEntity<GenericResponseDTO<StoricoStatoSezioneDTO>>> save(@RequestBody StoricoStatoSezioneDTO dto) {
        return storicoStatoSezioneService.save(dto)
            .map(ResponseEntity::ok);
    }
}
