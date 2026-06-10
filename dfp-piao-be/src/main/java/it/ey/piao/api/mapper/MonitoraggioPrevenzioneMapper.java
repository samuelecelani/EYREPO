package it.ey.piao.api.mapper;

import it.ey.dto.MonitoraggioPrevenzioneDTO;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MonitoraggioPrevenzione;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, IMisuraPrevenzioneEventoRischioMapper.class}
)
public interface MonitoraggioPrevenzioneMapper
{
    @Mapping(target = "idMisuraPrevenzioneEventoRischio", source = "misuraPrevenzioneEventoRischio.id")
    MonitoraggioPrevenzioneDTO toDto(MonitoraggioPrevenzione entity, @Context CycleAvoidingMappingContext context);

    MonitoraggioPrevenzione toEntity(MonitoraggioPrevenzioneDTO dto, @Context CycleAvoidingMappingContext context);
}
