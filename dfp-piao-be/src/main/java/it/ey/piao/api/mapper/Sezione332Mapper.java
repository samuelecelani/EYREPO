package it.ey.piao.api.mapper;

import it.ey.dto.Sezione332DTO;
import it.ey.entity.Sezione332;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, SezioneBaseMapper.class, ObiettiviRisultatiFotografiaMapper.class, AttivitaFormativeMapper.class}
)

public interface Sezione332Mapper
{

    @Mapping(target = "idPiao", source = "piao.id")
    @Mapping(target = "statoSezione", ignore = true)
    Sezione332DTO toDto(Sezione332 entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "piao.id", source = "idPiao")
    @Mapping(target = "idStato", ignore = true)
    Sezione332 toEntity(Sezione332DTO dto, @Context CycleAvoidingMappingContext context);

}
