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

    PiaoDTO toDto(Piao entity);

    /**
     * Mappa Piao entity → DTO con context
     */

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
