package it.ey.piao.api.mapper;

import it.ey.dto.MilestoneDTO;
import it.ey.entity.Milestone;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, PromemoriaMapper.class, SottofaseMonitoraggioMapper.class}
)
public interface MilestoneMapper
{
    @Mapping(target = "idPromemoria", source = "promemoria.id")
    @Mapping(target = "idSottofaseMonitoraggio", source = "sottofaseMonitoraggio.id")
    MilestoneDTO toDto(Milestone entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "promemoria.id", source = "idPromemoria")
    @Mapping(target = "sottofaseMonitoraggio.id", source = "idSottofaseMonitoraggio")
    Milestone toEntity(MilestoneDTO dto, @Context CycleAvoidingMappingContext context);

    List<MilestoneDTO> toDtoList(List<Milestone> entities, @Context CycleAvoidingMappingContext context);

    List<Milestone> toEntityList(List<MilestoneDTO> entities,@Context CycleAvoidingMappingContext context);

}
