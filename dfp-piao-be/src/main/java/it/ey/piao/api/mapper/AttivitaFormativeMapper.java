package it.ey.piao.api.mapper;

import it.ey.dto.AttivitaFormativeDTO;
import it.ey.entity.AttivitaFormative;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, TipologiaAttivitaMapper.class, AmbitoCompetenzaMapper.class, AreaTematicaMapper.class, Sezione332Mapper.class}
)

public interface AttivitaFormativeMapper
{
    @Mapping(target = "idTipologiaAttivita", source = "tipologiaAttivita.id")
    @Mapping(target = "idAmbitoCompetenza", source = "ambitoCompetenza.id")
    @Mapping(target = "idAreaTematica", source = "areaTematica.id")
    @Mapping(target = "idSezione332", source = "sezione332.id")
    AttivitaFormativeDTO toDto(AttivitaFormative entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "tipologiaAttivita.id", source = "idTipologiaAttivita")
    @Mapping(target = "ambitoCompetenza.id", source = "idAmbitoCompetenza")
    @Mapping(target = "areaTematica.id", source = "idAreaTematica")
    @Mapping(target = "sezione332.id", source = "idSezione332")
    AttivitaFormative toEntity(AttivitaFormativeDTO dto, @Context CycleAvoidingMappingContext context);
}
