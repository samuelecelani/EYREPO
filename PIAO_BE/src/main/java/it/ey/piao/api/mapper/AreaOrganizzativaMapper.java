package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per AreaOrganizzativa.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AreaOrganizzativaMapper {

    /**
     * Mappa AreaOrganizzativa entity → DTO
     */
    @Mapping(target = "idSezione1", source = "sezione1.id")
    AreaOrganizzativaDTO toDto(AreaOrganizzativa entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa AreaOrganizzativaDTO → AreaOrganizzativa entity
     */
    @Mapping(target = "sezione1.id", source = "idSezione1")
    AreaOrganizzativa toEntity(AreaOrganizzativaDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista AreaOrganizzativa entity → DTO
     */
    List<AreaOrganizzativaDTO> toDtoList(List<AreaOrganizzativa> entities, @Context CycleAvoidingMappingContext context);

    List<AreaOrganizzativa> toEntityList(List<AreaOrganizzativaDTO> dtos, @Context CycleAvoidingMappingContext context);
}
