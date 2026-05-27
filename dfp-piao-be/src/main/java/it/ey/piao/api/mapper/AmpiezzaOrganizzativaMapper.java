package it.ey.piao.api.mapper;

import it.ey.dto.AmpiezzaOrganizzativaDTO;
import it.ey.entity.AmpiezzaOrganizzativa;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per AmpiezzaOrganizzativa.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AmpiezzaOrganizzativaMapper {

    /**
     * Mappa AmpiezzaOrganizzativa entity → DTO
     */
    @Mapping(target = "idSezione31", source = "sezione31.id")
    AmpiezzaOrganizzativaDTO toDto(AmpiezzaOrganizzativa entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa AmpiezzaOrganizzativaDTO → AmpiezzaOrganizzativa entity
     */
    @Mapping(target = "sezione31.id", source = "idSezione31")
    AmpiezzaOrganizzativa toEntity(AmpiezzaOrganizzativaDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista AmpiezzaOrganizzativa entity → DTO
     */
    List<AmpiezzaOrganizzativaDTO> toDtoList(List<AmpiezzaOrganizzativa> entities, @Context CycleAvoidingMappingContext context);

    List<AmpiezzaOrganizzativa> toEntityList(List<AmpiezzaOrganizzativaDTO> dtos, @Context CycleAvoidingMappingContext context);
}
