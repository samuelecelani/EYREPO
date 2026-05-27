package it.ey.piao.bff.service;

import it.ey.dto.AmpiezzaOrganizzativaDTO;
import it.ey.dto.GenericResponseDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAmpiezzaOrganizzativaService {
    Mono<GenericResponseDTO<List<AmpiezzaOrganizzativaDTO>>> findByIdSezione31(Long idSezione31);
    Mono<GenericResponseDTO<AmpiezzaOrganizzativaDTO>> save(AmpiezzaOrganizzativaDTO request);
    Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione);
}
