package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per ObbiettivoPerformance e i suoi Indicatori annidati.
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, OVPStrategiaMapper.class}
)
public interface ObbiettivoPerformanceMapper {

    /**
     * Mappa ObbiettivoPerformance entity → DTO (INCLUDE GLI INDICATORI!)
     */
    @Mapping(target = "indicatori", source = "indicatori")
    @Mapping(target = "contributoreInterno", ignore = true)
    @Mapping(target = "idSezione22" , source = "sezione22.id")
    @Mapping(target ="idOvp", source = "ovp.id")
    @Mapping(target ="idStrategiaOvp", source = "ovpStrategia.id")
    ObbiettivoPerformanceDTO toDto(ObbiettivoPerformance entity, @Context CycleAvoidingMappingContext context);


    /**
     * Mappa ObbiettivoPerformanceDTO → ObbiettivoPerformance entity
     * IMPORTANTE: ovp e ovpStrategia sono ignorati e gestiti manualmente nel service
     */
    @Mapping(target = "sezione22", ignore = true) // Gestito manualmente nel service
    @Mapping(target = "ovp", ignore = true) // Gestito manualmente nel service
    @Mapping(target = "ovpStrategia", ignore = true) // Gestito manualmente nel service
    @Mapping(target = "indicatori", ignore = true) // Gestito dai metodi sync nel service
    @Mapping(target = "stakeholders", ignore = true) // Gestito dai metodi sync nel service
    ObbiettivoPerformance toEntity(ObbiettivoPerformanceDTO dto, @Context CycleAvoidingMappingContext context);

List<ObbiettivoPerformance> toEntityList(List<ObbiettivoPerformanceDTO> dtos, @Context CycleAvoidingMappingContext context);

//List<ObbiettivoPerformanceDTO>toDtoList(List<ObbiettivoPerformance> entities, @Context CycleAvoidingMappingContext context);


    /**
     * Mappa ObbiettivoPerformanceIndicatore entity → DTO
     * IMPORTANTE: Ignora obbiettivoPerformance per evitare riferimenti circolari
     */
    @Mapping(target = "obbiettivoPerformance", ignore = true)
    ObiettivoIndicatoriDTO toDto(ObiettivoPerformanceIndicatore entity, @Context CycleAvoidingMappingContext context);

    /*@Mapping(target = "obbiettivoPerformance", ignore = true)
    ObiettivoStakeHolderDTO toDto(ObiettivoPerformanceStakeHolder entity, @Context CycleAvoidingMappingContext context);*/

    /**
     * Mappa lista Indicatori entity → DTO
     */
    List<ObiettivoIndicatoriDTO> indicatoriToDto(List<ObiettivoPerformanceIndicatore> entities, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "obbiettivoPerformance", ignore = true)
    @Mapping(target = "stakeholder", source = "stakeholder")
    ObiettivoStakeHolderDTO stakeholderToDto(ObiettivoPerformanceStakeHolder entity, @Context CycleAvoidingMappingContext context);

    List<ObiettivoStakeHolderDTO> stakeHoldersToDto(List<ObiettivoPerformanceStakeHolder> entities, @Context CycleAvoidingMappingContext context);

}
