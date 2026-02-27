package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Fase e le sue relazioni (Attore, Attività MongoDB).
 * Usa CommonMapper per i mapping comuni.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface FaseMapper {

    /**
     * Mappa Fase entity → DTO
     */
    @Mapping(target = "attore", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "attivita", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "idSezione22", source = "sezione22.id")
    FaseDTO toDto(Fase entity,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa Fase DTO → entity
     */
    @Mapping(target = "sezione22", ignore = true) // Gestito manualmente
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    Fase toEntity(FaseDTO dto,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista Fase entity → DTO
     */
    List<FaseDTO> toDtoList(List<Fase> entities,@Context CycleAvoidingMappingContext context);

    // ========== Attore (MongoDB) ==========

    /**
     * Mappa Attore entity → DTO
     */
    AttoreDTO attoreToDto(Attore entity,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa AttoreDTO → entity
     */
    Attore attoreToEntity(AttoreDTO dto,@Context CycleAvoidingMappingContext context);

    // ========== Attivita (MongoDB) ==========

    /**
     * Mappa Attivita entity → DTO
     */
    AttivitaDTO attivitaToDto(Attivita entity,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa AttivitaDTO → entity
     */
    Attivita attivitaToEntity(AttivitaDTO dto,@Context CycleAvoidingMappingContext context);

}
