package it.ey.piao.api.mapper;

import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaDTO;
import it.ey.dto.ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO;
import it.ey.entity.ObiettivoPrevenzioneCorruzioneTrasparenza;
import it.ey.entity.ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, OVPMapper.class, IndicatoreMapper.class}
)
public interface IObiettivoPrevenzioneCorruzioneTrasparenzaMapper {

    /*
     * MapStruct non sa automaticamente estrarre l'id da un oggetto
     * Questi mapping servono a prendere l'id dalle relazioni (Sezione23, OVP, OVPStrategia, ObiettivoPerformance)
     * e copiarli nei campi Long corrispondenti del DTO
     * Quindi diciamo il Long per l'idSezione23 ottienilo dalla classe Sezione23 alla proprietà ID
     */
    @Mapping(target = "idSezione23", source = "sezione23.id")
    @Mapping(target = "idOVP", source = "ovp.id")
    @Mapping(target = "idStrategiaOVP", source = "ovpStrategia.id")
    @Mapping(target = "idObbiettivoPerformance", source = "obbiettivoPerformance.id")
    @Mapping(target = "indicatori", source = "indicatori")
    ObiettivoPrevenzioneCorruzioneTrasparenzaDTO toDto(ObiettivoPrevenzioneCorruzioneTrasparenza entity, @Context CycleAvoidingMappingContext context);


    /*
     * Ignora le relazioni che saranno valorizzate manualmente nel service
     * Campi tecnici come createdTs / updatedTs vengono gestiti dal db
     */
    @Mapping(target = "sezione23.id", source = "idSezione23")
    @Mapping(target = "ovp", ignore = true)
    @Mapping(target = "ovpStrategia", ignore = true)
    @Mapping(target = "obbiettivoPerformance", ignore = true)
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    ObiettivoPrevenzioneCorruzioneTrasparenza toEntity(ObiettivoPrevenzioneCorruzioneTrasparenzaDTO dto, @Context CycleAvoidingMappingContext context);

    List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> toDtoList(List<ObiettivoPrevenzioneCorruzioneTrasparenza> entities, @Context CycleAvoidingMappingContext context);

    List<ObiettivoPrevenzioneCorruzioneTrasparenza> toEntityList(List<ObiettivoPrevenzioneCorruzioneTrasparenzaDTO> dtos, @Context CycleAvoidingMappingContext context);


    // Mapping della tabella di giunzione

    @Mapping(target = "idObiettivoPrevenzioneCorruzioneTrasparenza", source = "obiettivoPrevenzioneCorruzioneTrasparenza.id")
    @Mapping(target = "indicatore", source = "indicatore")
    ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO toDto(ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori entity, @Context CycleAvoidingMappingContext context);
    ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori toEntity(ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO dto,@Context CycleAvoidingMappingContext context);


    List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO> indicatoriToDto(List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori> entities, @Context CycleAvoidingMappingContext context);
    List <ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatori> indicatoritoEntity(List<ObiettivoPrevenzioneCorruzioneTrasparenzaIndicatoriDTO>dto, @Context CycleAvoidingMappingContext context);
}
