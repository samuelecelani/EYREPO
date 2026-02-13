package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per OVPStrategia e Indicatori annidati.
 * Usa CycleAvoidingMappingContext per prevenire StackOverflowError da cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {IndicatoreMapper.class}
)
public interface OVPStrategiaMapper {

    /**
     * Mappa OVPStrategia entity → DTO (ignora indicatori - gestiti manualmente)
     */

    OVPStrategiaDTO toDto(OVPStrategia entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OVPStrategia DTO → entity
     */
    @Mapping(target = "ovp", ignore = true) // Gestito manualmente
    @Mapping(target = "obbiettiviPerformance", ignore = true)
    @Mapping(target = "indicatori", ignore = true) // Gestito manualmente
    OVPStrategia toEntity(OVPStrategiaDTO dto, @Context CycleAvoidingMappingContext context);


    /**
     * Mappa lista OVPStrategia entity → DTO
     */
    List<OVPStrategiaDTO> toDtoList(List<OVPStrategia> entities, @Context CycleAvoidingMappingContext context);
    List<OVPStrategia>toEntityList(List<OVPStrategiaDTO> dtos, @Context CycleAvoidingMappingContext context);
    /**
     * Mappa OVPStrategiaIndicatore entity → DTO
     * IMPORTANTE: Ignora ovpStrategia per evitare riferimenti circolari (StackOverflowError)
     */
    @Mapping(target = "ovpStrategia", ignore = true)
    OVPStrategiaIndicatoreDTO indicatoreToDto(OVPStrategiaIndicatore entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OVPStrategiaIndicatore DTO → entity
     */
    @Mapping(target = "ovpStrategia", ignore = true) // Gestito manualmente
    OVPStrategiaIndicatore indicatoreToEntity(OVPStrategiaIndicatoreDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista OVPStrategiaIndicatore entity → DTO
     */
    List<OVPStrategiaIndicatoreDTO> indicatoriToDto(List<OVPStrategiaIndicatore> entities, @Context CycleAvoidingMappingContext context);
}
