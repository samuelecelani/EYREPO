package it.ey.piao.api.mapper;

import it.ey.dto.CategoriaObiettiviDTO;
import it.ey.entity.CategoriaObiettivi;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface CategoriaObiettiviMapper {

    /**
     * DTO → Entity
     */
    @Mapping(target = "sezione4", ignore = true)
    @Mapping(target = "sottofase", ignore = true)
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    CategoriaObiettivi toEntity(CategoriaObiettiviDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Entity → DTO
     */
    @Mapping(source = "sezione4.id", target = "idSezione4")
    @Mapping(source = "sottofase.id", target = "idSottofase")
    @Mapping(target = "ulterioriInfo", ignore = true) // Mongo gestito nel service
    @Mapping(target = "attore", ignore = true)   // Mongo gestito nel service
    @Mapping(target = "attivita", ignore = true)  // Mongo gestito nel service
    CategoriaObiettiviDTO toDto(CategoriaObiettivi entity, @Context CycleAvoidingMappingContext context);

    List<CategoriaObiettiviDTO> toDtoList(List<CategoriaObiettivi> entities, @Context CycleAvoidingMappingContext context);
    List<CategoriaObiettivi> toEntityList(List<CategoriaObiettiviDTO> dtos, @Context CycleAvoidingMappingContext context);
}
