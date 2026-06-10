package it.ey.piao.api.mapper;

import it.ey.dto.Sezione32DTO;
import it.ey.entity.Sezione32;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, SezioneBaseMapper.class, TabellaFunzionaleMapper.class}
)

public interface Sezione32Mapper
{
    /**
     * Mappa Sezione32 entity → DTO (include tutte le relazioni annidate)
     */
    @Mapping(target = "statoSezione", ignore = true) // Gestito manualmente
    @Mapping(target = "idPiao", source = "piao.id")
    Sezione32DTO toDto(Sezione32 entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Sezione32 DTO → entity
     */
    @Mapping(target = "piao.id", source = "idPiao")
    @Mapping(target = "idStato", ignore = true) // Gestito manualmente
    Sezione32 toEntity(Sezione32DTO dto, @Context CycleAvoidingMappingContext context);
}
