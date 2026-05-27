package it.ey.piao.api.mapper;

import it.ey.dto.AreaTematicaDTO;
import it.ey.entity.AreaTematica;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface AreaTematicaMapper
{

    AreaTematicaDTO toDto(AreaTematica entity, @Context CycleAvoidingMappingContext context);

    AreaTematica toEntity(AreaTematicaDTO dto, @Context CycleAvoidingMappingContext context);

    List<AreaTematicaDTO> toDtoList(List<AreaTematica> entityList, @Context CycleAvoidingMappingContext context);

    List<AreaTematica> toEntityList(List<AreaTematicaDTO> dtoList, @Context CycleAvoidingMappingContext context);

}
