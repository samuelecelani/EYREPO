package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per StrutturaPiao.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface StrutturaPiaoMapper {

    /**
     * Mappa StrutturaPiao entity → DTO
     */
    @Mapping(target = "children", ignore = true) // Gestito manualmente nella gerarchia
    @Mapping(target = "statoSezione", ignore = true) // Calcolato dinamicamente
    @Mapping(target = "updatedTs", ignore = true) // Calcolato dinamicamente
    StrutturaPiaoDTO toDto(StrutturaPiao entity);

    /**
     * Mappa StrutturaPiaoDTO → StrutturaPiao entity
     */
    @Mapping(target = "createdTs", ignore = true)
    StrutturaPiao toEntity(StrutturaPiaoDTO dto);

    /**
     * Mappa lista StrutturaPiao entity → DTO
     */
    List<StrutturaPiaoDTO> toDtoList(List<StrutturaPiao> entities);
}
