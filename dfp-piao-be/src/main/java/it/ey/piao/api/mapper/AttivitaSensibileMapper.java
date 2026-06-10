package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per AttivitaSensibile e le sue relazioni (Attore, UlterioriInfo MongoDB).
 * Usa CommonMapper per i mapping comuni (UlterioriInfo).
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, EventoRischioMapper.class}
)
public interface AttivitaSensibileMapper {

    /**
     * Mappa AttivitaSensibile entity → DTO
     */
    @Mapping(target = "idSezione23", source = "sezione23.id")
    @Mapping(target = "ulterioriInfo", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "attore", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "eventoRischio", source = "eventoRischio")
    AttivitaSensibileDTO toDto(AttivitaSensibile entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa AttivitaSensibileDTO → AttivitaSensibile entity
     */
    @Mapping(target = "sezione23.id", source = "idSezione23") // Gestito manualmente
    @Mapping(target = "eventoRischio", ignore = true) // Gestito manualmente nel service con batch save
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    AttivitaSensibile toEntity(AttivitaSensibileDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista AttivitaSensibile entity → DTO
     */
    List<AttivitaSensibileDTO> toDtoList(List<AttivitaSensibile> entities, @Context CycleAvoidingMappingContext context);

    // ========== Attore (MongoDB) ==========

    /**
     * Mappa Attore entity → DTO
     */
    AttoreDTO attoreToDto(Attore entity);

    /**
     * Mappa AttoreDTO → entity
     */
    Attore attoreToEntity(AttoreDTO dto);

    /**
     * Mappa lista Attore entity → DTO
     */
    List<AttoreDTO> attoreToDtoList(List<Attore> entities);

    // UlterioriInfo mapping ereditato da CommonMapper:
    // - CommonMapper.ulterioriInfoDtoToEntity / ulterioriInfoEntityToDto
}
