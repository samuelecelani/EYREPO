package it.ey.piao.api.mapper;

import it.ey.dto.AmbitoCompetenzaDTO;
import it.ey.entity.AmbitoCompetenza;
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
public interface AmbitoCompetenzaMapper
{

    AmbitoCompetenzaDTO toDto(AmbitoCompetenza entity, @Context CycleAvoidingMappingContext context);

    AmbitoCompetenza toEntity(AmbitoCompetenzaDTO dto, @Context CycleAvoidingMappingContext context);

    List<AmbitoCompetenzaDTO> toDtoList(List<AmbitoCompetenza> entityList, @Context CycleAvoidingMappingContext context);

    List<AmbitoCompetenza> toEntityList(List<AmbitoCompetenzaDTO> dtoList, @Context CycleAvoidingMappingContext context);

}
