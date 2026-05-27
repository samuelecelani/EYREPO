package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per OVPRisorsaFinanziaria.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OVPRisorsaFinanziariaMapper {

    /**
     * Mappa OVPRisorsaFinanziaria entity → DTO
     */
    @Mapping(target = "idOvp", source = "ovp.id")
    OVPRisorsaFinanziariaDTO toDto(OVPRisorsaFinanziaria entity);

    /**
     * Mappa OVPRisorsaFinanziariaDTO → OVPRisorsaFinanziaria entity
     */
    @Mapping(target = "ovp.id", source = "idOvp") // Gestito manualmente
    OVPRisorsaFinanziaria toEntity(OVPRisorsaFinanziariaDTO dto);

    /**
     * Mappa lista OVPRisorsaFinanziaria entity → DTO
     */
    List<OVPRisorsaFinanziariaDTO> toDtoList(List<OVPRisorsaFinanziaria> entities);
}
