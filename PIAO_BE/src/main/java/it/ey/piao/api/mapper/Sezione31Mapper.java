package it.ey.piao.api.mapper;

import it.ey.dto.Sezione31DTO;
import it.ey.entity.Sezione31;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)

public interface Sezione31Mapper
{
    /**
     * Mappa Sezione23 entity → DTO (include tutte le relazioni annidate)
     */
    @Mapping(target = "statoSezione", ignore = true) // Gestito manualmente
    @Mapping(target = "idPiao", source = "piao.id")
    Sezione31DTO toDto(Sezione31 entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Sezione23 DTO → entity
     */
    @Mapping(target = "piao.id", source = "idPiao")
    @Mapping(target = "idStato", ignore = true) // Gestito manualmente
    Sezione31 toEntity(Sezione31DTO dto, @Context CycleAvoidingMappingContext context);
}
