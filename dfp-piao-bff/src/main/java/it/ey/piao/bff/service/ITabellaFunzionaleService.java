package it.ey.piao.bff.service;

import it.ey.dto.GenericResponseDTO;
import it.ey.dto.TabellaFunzionaleDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ITabellaFunzionaleService {
    Mono<GenericResponseDTO<List<TabellaFunzionaleDTO>>> findByIdEntitaFKAndCodTipologiaFK(Long idEntitaFK, String codTipologiaFK);
    Mono<GenericResponseDTO<TabellaFunzionaleDTO>> save(TabellaFunzionaleDTO request);
    Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String codTipologiaFK, String testoSezione);
}
