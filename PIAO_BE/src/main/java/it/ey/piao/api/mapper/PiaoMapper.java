package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Piao e le sue sezioni.
 * Usa CommonMapper per i mapping comuni.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface PiaoMapper {

    /**
     * Mappa Piao entity → DTO
     */
    @Mapping(target = "sezione1", ignore = true) // Gestito separatamente
    @Mapping(target = "sezione21", ignore = true) // Gestito separatamente
    @Mapping(target = "sezione22", ignore = true) // Gestito separatamente
    @Mapping(target = "sezione23", ignore = true) // Gestito separatamente
    PiaoDTO toDto(Piao entity);

    /**
     * Mappa Piao entity → DTO con context
     */
    @Mapping(target = "sezione1", ignore = true)
    @Mapping(target = "sezione21", ignore = true)
    @Mapping(target = "sezione22", ignore = true)
    @Mapping(target = "sezione23", ignore = true)
    PiaoDTO toDto(Piao entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa PiaoDTO → Piao entity
     */
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    Piao toEntity(PiaoDTO dto);

    /**
     * Mappa PiaoDTO → Piao entity con context
     */
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    Piao toEntity(PiaoDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista Piao entity → DTO
     */
    List<PiaoDTO> toDtoList(List<Piao> entities);
}
