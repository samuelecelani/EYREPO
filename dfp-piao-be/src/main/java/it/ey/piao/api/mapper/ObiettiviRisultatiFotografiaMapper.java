package it.ey.piao.api.mapper;

import it.ey.dto.ObiettiviRisultatiFotografiaDTO;
import it.ey.entity.ObiettiviRisultatiFotografia;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {
        CommonMapper.class,
        TipologiaAttivitaMapper.class,
        AmbitoCompetenzaMapper.class,
        AreaTematicaMapper.class,
        TipologiaDestinatariMapper.class,
        Sezione332Mapper.class
    }
)
public interface ObiettiviRisultatiFotografiaMapper {

    @Mapping(target = "idSezione332", source = "sezione332.id")
    @Mapping(target = "idTipologiaAttivita", source = "tipologiaAttivita.id")
    @Mapping(target = "idAmbitoCompetenza", source = "ambitoCompetenza.id")
    @Mapping(target = "idAreaTematica", source = "areaTematica.id")
    @Mapping(target = "idTipologiaDestinatari", source = "tipologiaDestinatari.id")
    ObiettiviRisultatiFotografiaDTO toDto(ObiettiviRisultatiFotografia entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "sezione332.id", source = "idSezione332")
    @Mapping(target = "tipologiaAttivita.id", source = "idTipologiaAttivita")
    @Mapping(target = "ambitoCompetenza.id", source = "idAmbitoCompetenza")
    @Mapping(target = "areaTematica.id", source = "idAreaTematica")
    @Mapping(target = "tipologiaDestinatari.id", source = "idTipologiaDestinatari")
    ObiettiviRisultatiFotografia toEntity(ObiettiviRisultatiFotografiaDTO dto, @Context CycleAvoidingMappingContext context);

}
