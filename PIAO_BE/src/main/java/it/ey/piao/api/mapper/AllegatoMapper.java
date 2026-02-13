package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per Allegato e Logo (MongoDB).
 * Usa CommonMapper per i mapping comuni.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface AllegatoMapper {

    /**
     * Mappa Allegato entity → DTO
     */
    @Mapping(target = "logo", ignore = true) // MongoDB - gestito separatamente
    AllegatoDTO toDto(Allegato entity);

    /**
     * Mappa AllegatoDTO → Allegato entity
     */
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    Allegato toEntity(AllegatoDTO dto);

    /**
     * Mappa lista Allegato entity → DTO
     */
    List<AllegatoDTO> toDtoList(List<Allegato> entities);

    // ========== Logo (MongoDB) ==========

    /**
     * Mappa Logo entity → DTO
     */
    LogoDTO logoToDto(Logo entity);

    /**
     * Mappa LogoDTO → entity
     */
    Logo logoToEntity(LogoDTO dto);
}
