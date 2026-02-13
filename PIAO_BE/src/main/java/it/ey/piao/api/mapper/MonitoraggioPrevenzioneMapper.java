package it.ey.piao.api.mapper;

import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MonitoraggioPrevenzione;
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
    uses = {CommonMapper.class, IMisuraPrevenzioneEventoRischioMapper.class}
)
public interface MonitoraggioPrevenzioneMapper
{
    MonitoraggioPrevenzioneDTO toDto(MonitoraggioPrevenzione entity, @Context CycleAvoidingMappingContext context);

    MonitoraggioPrevenzione toEntity(MonitoraggioPrevenzioneDTO dto, @Context CycleAvoidingMappingContext context);
}
