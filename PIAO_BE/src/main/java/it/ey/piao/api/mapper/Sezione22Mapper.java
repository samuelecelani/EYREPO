package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;


/**
 * MapStruct mapper per Sezione22 e tutte le sue relazioni annidate.
 * Include: Obiettivi Performance (con Indicatori), Fasi (con Attore/Attività), Adempimenti
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 * Usa SOLO CommonMapper centralizzato per evitare duplicazione di logica.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class,ObbiettivoPerformanceMapper.class,FaseMapper.class,AdempimentoMapper.class}
)
public interface Sezione22Mapper {

    /**
     * Mappa Sezione22 entity → DTO (include tutte le relazioni annidate)
     */
    @Mapping(target = "obbiettiviPerformance", source = "obbiettiviPerformance")
    @Mapping(target = "adempimenti", source = "adempimenti")
    @Mapping(target = "idPiao", source = "piao.id")
    @Mapping(target = "statoSezione", ignore = true) // Gestito manualmente
    @Mapping(target = "ulterioriInfo", ignore = true) // MongoDB - gestito separatamente
    Sezione22DTO toDto(Sezione22 entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Sezione22 DTO → entity
     */
    @Mapping(target = "piao.id", source = "idPiao")
    @Mapping(target = "idStato", ignore = true) // Gestito manualmente
    Sezione22 toEntity(Sezione22DTO dto, @Context CycleAvoidingMappingContext context);
}
