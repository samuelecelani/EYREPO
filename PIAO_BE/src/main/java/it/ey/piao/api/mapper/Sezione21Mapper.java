package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;


/**
 * MapStruct mapper per Sezione21 e tutte le sue relazioni annidate.
 * MapStruct gestisce correttamente le relazioni profonde (OVP → Strategie → Indicatori)
 * che GenericMapper/ModelMapper non riesce a mappare.
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 * Usa SOLO CommonMapper per evitare duplicazione di logica.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class,OVPMapper.class,FondiEuropeiMapper.class,ProceduraMapper.class}
)
public interface Sezione21Mapper {

    /**
     * Mappa Sezione21 entity → DTO (incluse tutte le relazioni annidate)
     */
    @Mapping(target = "ovp", source = "ovpList")
    @Mapping(target = "idPiao", source = "piao.id") // Mappa idPiao da piao.id
    @Mapping(target = "statoSezione", ignore = true) // Gestito manualmente
    @Mapping(target = "swotPuntiForza", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "swotPuntiDebolezza", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "swotOpportunita", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "swotMinacce", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "ulterioriInfo", ignore = true) // MongoDB - gestito separatamente
    Sezione21DTO toDto(Sezione21 entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Sezione21 DTO → entity
     */
    @Mapping(target = "ovpList", source = "ovp")
    @Mapping(target = "piao.id", source = "idPiao") // Gestito manualmente nel service
    @Mapping(target = "idStato", ignore = true) // Gestito manualmente
    Sezione21 toEntity(Sezione21DTO dto, @Context CycleAvoidingMappingContext context);



}
