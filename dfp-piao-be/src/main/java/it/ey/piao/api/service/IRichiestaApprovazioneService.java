package it.ey.piao.api.service;

import it.ey.dto.RichiestaApprovazioneDTO;

public interface IRichiestaApprovazioneService {

    void  saveOrUpdate(RichiestaApprovazioneDTO request);
    RichiestaApprovazioneDTO findByIdPiao(Long idPiao);
}
