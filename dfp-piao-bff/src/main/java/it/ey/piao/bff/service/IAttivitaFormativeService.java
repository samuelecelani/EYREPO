package it.ey.piao.bff.service;

import it.ey.dto.AttivitaFormativeDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IAttivitaFormativeService
{
    Mono<GenericResponseDTO<AttivitaFormativeDTO>> saveOrUpdate(AttivitaFormativeDTO attivitaFormativeDTO);

    Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione);
}
