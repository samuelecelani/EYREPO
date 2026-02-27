package it.ey.piao.api.mapper;

import it.ey.dto.SottofaseMonitoraggioDTO;
import it.ey.entity.SottofaseMonitoraggio;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, MilestoneMapper.class}
)
public interface SottofaseMonitoraggioMapper {
    /**
     * DTO → Entity
     */
    @Mapping(target = "sezione4", ignore = true)
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    SottofaseMonitoraggio toEntity(SottofaseMonitoraggioDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Entity → DTO
     */

    @Mapping(source = "sezione4.id", target = "idSezione4")
    @Mapping(target = "attore", ignore = true) // Mongo gestito nel service
    SottofaseMonitoraggioDTO toDto(SottofaseMonitoraggio entity, @Context CycleAvoidingMappingContext context);


    List<SottofaseMonitoraggioDTO> toDtoList(List<SottofaseMonitoraggio> entities, @Context CycleAvoidingMappingContext context);
    List<SottofaseMonitoraggio> toEntityList(List<SottofaseMonitoraggioDTO> dto, @Context CycleAvoidingMappingContext context);
}
