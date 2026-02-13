package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per TipologiaAndamentoValoreIndicatore.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TipologiaAndamentoValoreIndicatoreMapper {

    /**
     * Mappa TipologiaAndamentoValoreIndicatore entity → DTO
     */
    TipologiaAndamentoValoreIndicatoreDTO toDto(TipologiaAndamentoValoreIndicatore entity);

    /**
     * Mappa TipologiaAndamentoValoreIndicatoreDTO → entity
     */
    TipologiaAndamentoValoreIndicatore toEntity(TipologiaAndamentoValoreIndicatoreDTO dto);

    /**
     * Mappa lista TipologiaAndamentoValoreIndicatore entity → DTO
     */
    List<TipologiaAndamentoValoreIndicatoreDTO> toDtoList(List<TipologiaAndamentoValoreIndicatore> entities);
}
