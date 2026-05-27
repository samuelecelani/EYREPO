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
    uses = {MisuraPrevenzioneMapper.class,IndicatoreMapper.class}
)
public interface ObiettivoPrevenzioneMapper {

    /**
     * Mappa ObiettivoPrevenzione entity → DTO
     * NOTA: Le misure non sono mappate qui perché la relazione è gestita
     * dal service (MisuraPrevenzioneService.getAllByObiettivoPrevenzione)
     */
    @Mapping(target = "misurePrevenzione", ignore = true) // Gestito dal service
    @Mapping(target = "idSezione23", source = "sezione23.id") // Mappa l'ID della sezione
    ObiettivoPrevenzioneDTO toDto(ObiettivoPrevenzione entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa ObiettivoPrevenzione DTO → entity
     */
    @Mapping(target = "sezione23.id" , source ="idSezione23" ) // Gestito manualmente
    ObiettivoPrevenzione toEntity(ObiettivoPrevenzioneDTO dto, @Context CycleAvoidingMappingContext context);


    ObiettivoPrevenzioneIndicatoreDTO toIndicatoreDto(ObiettivoPrevenzioneIndicatore entity, @Context CycleAvoidingMappingContext context);

    ObiettivoPrevenzioneIndicatore toIndicatoreEntity(ObiettivoPrevenzioneIndicatoreDTO dto, @Context CycleAvoidingMappingContext context);

    List<ObiettivoPrevenzioneIndicatoreDTO> toIndicatoreDtoList(List<ObiettivoPrevenzioneIndicatore> entities, @Context CycleAvoidingMappingContext context);

    List<ObiettivoPrevenzioneIndicatore> toIndicatoreEntityList(List<ObiettivoPrevenzioneIndicatoreDTO> dtos, @Context CycleAvoidingMappingContext context);
    /**
     * Mappa lista ObiettivoPrevenzione entity → DTO
     */
    List<ObiettivoPrevenzioneDTO> toDtoList(List<ObiettivoPrevenzione> entities, @Context CycleAvoidingMappingContext context);
    List<ObiettivoPrevenzione> toEntityList(List<ObiettivoPrevenzioneDTO> dtos, @Context CycleAvoidingMappingContext context);
}
