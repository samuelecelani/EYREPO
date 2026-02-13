package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

/**
 * MapStruct mapper per Sezione1 e tutte le sue relazioni.
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses ={ CommonMapper.class, PrioritaPoliticaMapper.class, AreaOrganizzativaMapper.class,}

)
public interface Sezione1Mapper {

    /**
     * Mappa Sezione1 entity → DTO
     */
    @Mapping(target = "statoSezione", ignore = true) // Gestito manualmente
    @Mapping(target = "ulterioriInfoDTO", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "social", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "idPiao", source = "piao.id") // Piao
    //@Mapping(target = "stakeHolders", ignore = true) // Gestito separatamente (appartiene al Piao)
    Sezione1DTO toDto(Sezione1 entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Sezione1 DTO → entity
     */
    @Mapping(target = "piao.id", source = "idPiao") // Piao
    @Mapping(target = "idStato", ignore = true) // Gestito manualmente
    Sezione1 toEntity(Sezione1DTO dto, @Context CycleAvoidingMappingContext context);
}
