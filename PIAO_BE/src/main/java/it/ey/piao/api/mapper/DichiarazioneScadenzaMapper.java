package it.ey.piao.api.mapper;

import it.ey.dto.DichiarazioneScadenzaDTO;
import it.ey.entity.DichiarazioneScadenza;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, MotivazioneDichiarazioneMapper.class}
)
public interface DichiarazioneScadenzaMapper
{
    @Mapping(target = "idPiao", source = "piao.id")
    @Mapping(target = "idMotivazioneDichiarazione", source = "motivazioneDichiarazione.id")
    DichiarazioneScadenzaDTO toDto(DichiarazioneScadenza entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "piao.id", source = "idPiao")
    @Mapping(target = "motivazioneDichiarazione.id", source = "idMotivazioneDichiarazione")
    DichiarazioneScadenza toEntity(DichiarazioneScadenzaDTO dto, @Context CycleAvoidingMappingContext context);
}
