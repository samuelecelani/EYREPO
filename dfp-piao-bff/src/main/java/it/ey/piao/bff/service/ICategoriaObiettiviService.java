package it.ey.piao.bff.service;

import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.dto.CategoriaObiettiviTipDTO;
import it.ey.dto.GenericResponseDTO;
import it.ey.enums.CodTipologiaCategoria;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICategoriaObiettiviService {
    Mono<GenericResponseDTO<CategoriaObiettiviDTO>> saveOrUpdate(CategoriaObiettiviDTO request);

    Mono<GenericResponseDTO<List<CategoriaObiettiviDTO>>> getAllBySezione4(Long idSezione4);

    Mono<Void> deleteById(Long id, String campiModificati, Long idPiao, String testoSezione);

    Mono<GenericResponseDTO<List<CategoriaObiettiviTipDTO>>> getAllCategoriaObiettiviTipPerCodTipologiaFK(CodTipologiaCategoria codTipologiaFK);
 }
