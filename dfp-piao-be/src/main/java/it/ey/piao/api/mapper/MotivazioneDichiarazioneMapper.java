package it.ey.piao.api.mapper;

import it.ey.dto.MotivazioneDichiarazioneDTO;
import it.ey.entity.MotivazioneDichiarazione;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface MotivazioneDichiarazioneMapper
{
    MotivazioneDichiarazioneDTO toDto(MotivazioneDichiarazione entity, @Context CycleAvoidingMappingContext context);
    MotivazioneDichiarazione toEntity(MotivazioneDichiarazioneDTO dto, @Context CycleAvoidingMappingContext context);
}
