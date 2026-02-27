package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.RichiestaApprovazioneDTO;
import reactor.core.publisher.Mono;

public interface IRichiestaApprovazioneService {
    public Mono<GenericResponseDTO<Void>> saveOrUpdate(RichiestaApprovazioneDTO request);
    public Mono<GenericResponseDTO<RichiestaApprovazioneDTO>> findByPiao(Long idPiao);
}
