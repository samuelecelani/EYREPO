package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Indicatore e le sue relazioni (TipologiaAndamentoValoreIndicatore).
 * Usa CommonMapper per i mapping comuni (UlterioriInfo, etc.)
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface IndicatoreMapper {

    /**
     * Mappa Indicatore entity → DTO
     */
    @Mapping(target = "idPiao", source = "piao.id")
    IndicatoreDTO toDto(Indicatore entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa IndicatoreDTO → Indicatore entity
     */
    @Mapping(target = "piao", ignore = true) // Gestito manualmente
    Indicatore toEntity(IndicatoreDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista Indicatore entity → DTO
     */
    List<IndicatoreDTO> toDtoList(List<Indicatore> entities, @Context CycleAvoidingMappingContext context);
    List<Indicatore> toEntityList(List<IndicatoreDTO> dtos, @Context CycleAvoidingMappingContext context);

    // ========== TipologiaAndamentoValoreIndicatore ==========

    /**
     * Mappa TipologiaAndamentoValoreIndicatore entity → DTO
     */
    TipologiaAndamentoValoreIndicatoreDTO tipologiaToDto(TipologiaAndamentoValoreIndicatore entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa TipologiaAndamentoValoreIndicatoreDTO → entity
     */
    TipologiaAndamentoValoreIndicatore tipologiaToEntity(TipologiaAndamentoValoreIndicatoreDTO dto, @Context CycleAvoidingMappingContext context);
}
