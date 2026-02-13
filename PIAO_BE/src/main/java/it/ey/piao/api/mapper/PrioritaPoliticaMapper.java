package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per PrioritaPolitica.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PrioritaPoliticaMapper {

    /**
     * Mappa PrioritaPolitica entity → DTO
     */
    @Mapping(target = "idSezione1", source = "sezione1.id")
    PrioritaPoliticaDTO toDto(PrioritaPolitica entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa PrioritaPoliticaDTO → PrioritaPolitica entity
     */
    @Mapping(target = "sezione1.id", source = "idSezione1")
    PrioritaPolitica toEntity(PrioritaPoliticaDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista PrioritaPolitica entity → DTO
     */
    List<PrioritaPoliticaDTO> toDtoList(List<PrioritaPolitica> entities, @Context CycleAvoidingMappingContext context);
    List<PrioritaPolitica> toEntityList(List<PrioritaPoliticaDTO> dtos, @Context CycleAvoidingMappingContext context);
}
