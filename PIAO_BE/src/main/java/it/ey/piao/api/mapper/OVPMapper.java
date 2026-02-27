package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper per OVP e tutte le sue relazioni annidate.
 * Include il mapping di Strategie e Indicatori.
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {OVPStrategiaMapper.class, StakeHolderMapper.class,AreaOrganizzativaMapper.class,
            PrioritaPoliticaMapper.class,OVPRisorsaFinanziariaMapper.class,OVPStrategiaMapper.class
    }
)
public interface OVPMapper {

    /**
     * Mappa OVP entity → DTO (incluse Strategie e Indicatori annidati)
     */
    @Mapping(target = "ovpStrategias", source = "ovpStrategias")
    @Mapping(target = "areeOrganizzative", source = "areeOrganizzative")
    @Mapping(target = "sezione21Id", source = "sezione21.id") // Mappa idSezione21 da sezione21.id
    OVPDTO toDto(OVP entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa OVP DTO → entity
     * IMPORTANTE: Tutte le liste OneToMany sono ignorate perché gestite manualmente
     * dai metodi sync* nel service che impostano correttamente il back-reference ovp
     */


    @Mapping(target = "sezione21.id", source = "sezione21Id")
    OVP toEntity(OVPDTO dto, @Context CycleAvoidingMappingContext context);


    @Mapping(target = "ovp", ignore = true) // Evita cicli infiniti
    OVPStakeHolderDTO ovpStakeHolderToDto(OVPStakeHolder entity, @Context CycleAvoidingMappingContext context);


    @Mapping(target = "ovp", ignore = true)
    OVPStakeHolder ovpStakeHolderDtoToEntity(OVPStakeHolderDTO dto, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "ovp", ignore = true)
    OVPPrioritaPolitica ovpPrioritaPoliticaToEntity(OVPPrioritaPoliticaDTO dto, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "ovp", ignore = true)
    OVPPrioritaPoliticaDTO ovpPrioritaPoliticaToDto(OVPPrioritaPolitica entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "ovp", ignore = true)
    OVPAreaOrganizzativa ovpAreaOrganizzativaToEntity(OVPAreaOrganizzativaDTO dto, @Context CycleAvoidingMappingContext context);
    @Mapping(target = "ovp", ignore = true)
    OVPAreaOrganizzativaDTO ovpAreaOrganizzativaToDto(OVPAreaOrganizzativa entity, @Context CycleAvoidingMappingContext context);




    List<OVPAreaOrganizzativa> ovpAreaOrganizzativaDtoListToEntityList(List<OVPAreaOrganizzativaDTO> dtos, @Context CycleAvoidingMappingContext context);
    List<OVPAreaOrganizzativaDTO> ovpAreaOrganizzativaEntityListToDtoList(List<OVPAreaOrganizzativa> entities, @Context CycleAvoidingMappingContext context);
    List<OVPPrioritaPolitica> ovpPrioritaPoliticaDtoListToEntityList(List<OVPPrioritaPoliticaDTO> dtos, @Context CycleAvoidingMappingContext context);
    List<OVPPrioritaPoliticaDTO> ovpPrioritaPoliticaEntityListToDtoList(List<OVPPrioritaPolitica> entities, @Context CycleAvoidingMappingContext context);
    List<OVPStakeHolder> ovpStakeHolderDtoListToEntityList(List<OVPStakeHolderDTO> dtos, @Context CycleAvoidingMappingContext context);
    List<OVPStakeHolderDTO> ovpStakeHolderEntityListToDtoList(List<OVPStakeHolder> entities, @Context CycleAvoidingMappingContext context);


    /**
     * Mappa lista OVP entity → DTO
     */
    List<OVPDTO> toDtoList(List<OVP> entities, @Context CycleAvoidingMappingContext context);
    List<OVP> toEntityList(List<OVPDTO> dtos, @Context CycleAvoidingMappingContext context);


}
