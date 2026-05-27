package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;


/**
 * MapStruct mapper per MisuraPrevenzione e i suoi Indicatori annidati.
 * NON usa CommonMapper per evitare ambiguità - tutti i mapping sono definiti qui.
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, IndicatoreMapper.class,StakeHolderMapper.class}
)
public interface MisuraPrevenzioneMapper {

    /**
     * Mappa MisuraPrevenzione entity → DTO (senza context, ignora indicatori)
     */
    @Mapping(source = "sezione23.id", target = "idSezione23")
    @Mapping(source = "obiettivoPrevenzione.id", target = "idObiettivoPrevenzione")
    @Mapping(source = "indicatori", target = "indicatori")
    @Mapping(source = "stakeholder", target = "stakeholder")
    MisuraPrevenzioneDTO toDto(MisuraPrevenzione entity,@Context CycleAvoidingMappingContext context);

    /**
     * Mappa MisuraPrevenzione DTO → entity (senza context)
     */

    @Mapping(source = "idSezione23", target = "sezione23.id")
    MisuraPrevenzione toEntity(MisuraPrevenzioneDTO dto,@Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzioneIndicatore> toEntityIndicatori(List<MisuraPrevenzioneIndicatore> dtos,@Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzioneDTO> toDtoList(List<MisuraPrevenzione> entities,@Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzione> toEntityList(List<MisuraPrevenzioneDTO> dtos,@Context CycleAvoidingMappingContext context);




}
