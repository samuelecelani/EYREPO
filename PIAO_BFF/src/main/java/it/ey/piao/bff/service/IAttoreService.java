package it.ey.piao.bff.service;

import it.ey.dto.AttoreDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.enums.Sezione;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IAttoreService {
    Mono<GenericResponseDTO<List<AttoreDTO>>> findListByIdPiao(Long idPiao);
    Mono<GenericResponseDTO<AttoreDTO>> save(Long idPiao, AttoreDTO attore);
    Mono<GenericResponseDTO<AttoreDTO>> findByExternalIdAndTipoSezione(Long externalId, Sezione tipoSezione);
}
