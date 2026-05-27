package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

public interface IOVPRisorsaFinanziariaService {
    Mono<GenericResponseDTO<Void>> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione);

}
