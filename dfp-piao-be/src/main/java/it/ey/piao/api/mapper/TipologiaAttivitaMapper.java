package it.ey.piao.api.mapper;

import it.ey.dto.TipologiaAttivitaDTO;
import it.ey.entity.TipologiaAttivita;
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
public interface TipologiaAttivitaMapper
{

    TipologiaAttivitaDTO toDto(TipologiaAttivita entity, @Context CycleAvoidingMappingContext context);

    TipologiaAttivita toEntity(TipologiaAttivitaDTO dto, @Context CycleAvoidingMappingContext context);

    List<TipologiaAttivitaDTO> toDtoList(List<TipologiaAttivita> entityList, @Context CycleAvoidingMappingContext context);

    List<TipologiaAttivita> toEntityList(List<TipologiaAttivitaDTO> dtoList, @Context CycleAvoidingMappingContext context);

}
