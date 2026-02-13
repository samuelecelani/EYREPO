package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per ObiettivoPrevenzione e le sue relazioni annidate (Misure Prevenzione con Indicatori).
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {MisuraPrevenzioneMapper.class}
)
public interface ObiettivoPrevenzioneMapper {

    /**
     * Mappa ObiettivoPrevenzione entity → DTO
     * NOTA: Le misure non sono mappate qui perché la relazione è gestita
     * dal service (MisuraPrevenzioneService.getAllByObiettivoPrevenzione)
     */
    @Mapping(target = "misurePrevenzione", ignore = true) // Gestito dal service
    ObiettivoPrevenzioneDTO toDto(ObiettivoPrevenzione entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa ObiettivoPrevenzione DTO → entity
     */
    @Mapping(target = "sezione23", ignore = true) // Gestito manualmente
    ObiettivoPrevenzione toEntity(ObiettivoPrevenzioneDTO dto, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa lista ObiettivoPrevenzione entity → DTO
     */
    List<ObiettivoPrevenzioneDTO> toDtoList(List<ObiettivoPrevenzione> entities, @Context CycleAvoidingMappingContext context);
}
