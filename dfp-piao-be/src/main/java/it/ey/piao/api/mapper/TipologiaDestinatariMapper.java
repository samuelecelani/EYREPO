package it.ey.piao.api.mapper;

import it.ey.dto.TipologiaDestinatariDTO;
import it.ey.entity.TipologiaDestinatari;
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
public interface TipologiaDestinatariMapper
{

    TipologiaDestinatariDTO toDto(TipologiaDestinatari entity, @Context CycleAvoidingMappingContext context);

    TipologiaDestinatari toEntity(TipologiaDestinatariDTO dto, @Context CycleAvoidingMappingContext context);

    List<TipologiaDestinatariDTO> toDtoList(List<TipologiaDestinatari> entityList, @Context CycleAvoidingMappingContext context);

    List<TipologiaDestinatari> toEntityList(List<TipologiaDestinatariDTO> dtoList, @Context CycleAvoidingMappingContext context);

}
