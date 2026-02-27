package it.ey.piao.api.controller.rest;

import it.ey.common.annotation.ApiV1Controller;
import it.ey.dto.StatoPIAODTO;
import it.ey.dto.StatoSezioneDTO;
import it.ey.piao.api.service.IStatoPIAOService;
import it.ey.piao.api.service.IStatoSezioneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiV1Controller("/stato")
public class StatoController {

    private final IStatoPIAOService servicePiao;
    private final IStatoSezioneService serviceSezione;

    @Autowired
    public StatoController(IStatoPIAOService servicePiao, IStatoSezioneService serviceSezione) {
        this.servicePiao = servicePiao;
        this.serviceSezione = serviceSezione;
    }

    @GetMapping("/piao")
    public List<StatoPIAODTO> getAllStatoPiao() {
        return servicePiao.findAll();
    }

    @GetMapping("/sezione")
    public List<StatoSezioneDTO> getAllStatoSezione() {
        return serviceSezione.findAll();
    }
}
