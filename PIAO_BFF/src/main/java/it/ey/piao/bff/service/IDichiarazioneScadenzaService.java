package it.ey.piao.bff.service;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IDichiarazioneScadenzaService
{
    Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> saveOrUpdate(DichiarazioneScadenzaDTO dto);
    Mono<Void> deleteById(Long id);
    Mono<GenericResponseDTO<DichiarazioneScadenzaDTO>> getExistingDichiarazioneScadenza(String codPAFK);

}
