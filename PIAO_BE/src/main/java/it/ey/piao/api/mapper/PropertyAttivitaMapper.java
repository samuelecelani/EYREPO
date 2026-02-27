package it.ey.piao.api.mapper;

import it.ey.dto.PropertyAttivitaDTO;
import it.ey.entity.PropertyAttivita;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PropertyAttivitaMapper {

    /**
     * DTO → Entity
     */
    PropertyAttivita toEntity(PropertyAttivitaDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Entity → DTO
     */
    PropertyAttivitaDTO toDto(PropertyAttivita entity, @Context CycleAvoidingMappingContext context);

    List<PropertyAttivitaDTO> toDtoList(List<PropertyAttivita> entities, @Context CycleAvoidingMappingContext context);
    List<PropertyAttivita> toEntityList(List<PropertyAttivitaDTO> dtos, @Context CycleAvoidingMappingContext context);
}
