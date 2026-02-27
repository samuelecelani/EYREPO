package it.ey.piao.api.controller.rest;


import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.RichiestaApprovazioneDTO;
import it.ey.piao.api.service.IRichiestaApprovazioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@ApiV1Controller("/richiesta-approvazione")
public class RichiestaApprovazioneController {

    private final IRichiestaApprovazioneService richiestaApprovazioneService;

    public RichiestaApprovazioneController(IRichiestaApprovazioneService richiestaApprovazioneService) {
        this.richiestaApprovazioneService = richiestaApprovazioneService;
    }

    @PostMapping("/save")
    public ResponseEntity<Void> save(@RequestBody RichiestaApprovazioneDTO request) {
      richiestaApprovazioneService.saveOrUpdate(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{idPiao}")
    public ResponseEntity<RichiestaApprovazioneDTO> getByIdPiao(@PathVariable Long idPiao) {
        return ResponseEntity.ok(richiestaApprovazioneService.findByIdPiao(idPiao));
    }



}
