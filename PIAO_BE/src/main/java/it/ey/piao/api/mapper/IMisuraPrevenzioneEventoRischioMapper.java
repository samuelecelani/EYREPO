package it.ey.piao.api.mapper;


import it.ey.dto.MisuraPrevenzioneEventoRischioDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioIndicatoreDTO;
import it.ey.dto.MisuraPrevenzioneEventoRischioStakeholderDTO;
import it.ey.entity.MisuraPrevenzioneEventoRischio;
import it.ey.entity.MisuraPrevenzioneEventoRischioIndicatore;
import it.ey.entity.MisuraPrevenzioneEventoRischioStakeholder;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class,MonitoraggioPrevenzioneMapper.class,IndicatoreMapper.class, StakeHolderMapper.class}
)
public interface IMisuraPrevenzioneEventoRischioMapper {


    /* MapStruct non sa automaticamente estrarre l'id da un oggetto
    // Questi mapping servono a prendere l'id dalle relazioni (eventoRischio e obiettivoPrevenzione)
     e copiarli nei campi Long corrispondenti del DTO (idEventoRischio, idObiettivoPrevenzione)*/
    @Mapping(target = "idEventoRischio", source = "eventoRischio.id")
    @Mapping(target = "idObiettivoPrevenzioneCorruzioneTrasparenza", source = "obiettivoPrevenzioneCorruzioneTrasparenza.id")
    @Mapping(target = "indicatori", source = "indicatori")
    @Mapping(target = "stakeholder", source = "stakeholder")
    @Mapping(target = "monitoraggioPrevenzione", source = "monitoraggioPrevenzione")
    MisuraPrevenzioneEventoRischioDTO toDto(MisuraPrevenzioneEventoRischio entity, @Context CycleAvoidingMappingContext context);


    //  Ignora le relazioni che saranno valorizzate manualmente nel service
    // quei campi tecnici sono gestiti dal db
    @Mapping(target = "eventoRischio", ignore = true)
    @Mapping(target = "obiettivoPrevenzioneCorruzioneTrasparenza", ignore = true)
    @Mapping(target = "indicatori", ignore = true)  // Gestiti dai metodi sync nel service
    @Mapping(target = "stakeholder", ignore = true)  // Gestiti dai metodi sync nel service
    @Mapping(target = "monitoraggioPrevenzione", ignore = true)  // Gestiti dai metodi sync nel service
    @Mapping(target = "createdTs", ignore = true)
    @Mapping(target = "updatedTs", ignore = true)
    MisuraPrevenzioneEventoRischio toEntity(MisuraPrevenzioneEventoRischioDTO entity, @Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzioneEventoRischioDTO> toDtoList(List<MisuraPrevenzioneEventoRischio> entities, @Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzioneEventoRischio> toEntityList(List<MisuraPrevenzioneEventoRischioDTO> dto,@Context CycleAvoidingMappingContext context);


    // --- MAPPER INDICATORI ---
    @Mapping(target = "indicatore", source = "indicatore")
    @Mapping(target = "misuraPrevenzioneEventoRischioDTO", ignore = true)  // Evita riferimenti circolari
    MisuraPrevenzioneEventoRischioIndicatoreDTO toDto(MisuraPrevenzioneEventoRischioIndicatore entity, @Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzioneEventoRischioIndicatoreDTO> indicatoriToDto(List<MisuraPrevenzioneEventoRischioIndicatore> entities, @Context CycleAvoidingMappingContext context);


    // --- MAPPER STAKEHOLDER ---
    @Mapping(target = "stakeholder", source = "stakeholder")  // Mappa stakeholder entity -> stakeholderDTO
    @Mapping(target = "misuraPrevenzioneEventoRischioDTO", ignore = true)  // Evita riferimenti circolari
    MisuraPrevenzioneEventoRischioStakeholderDTO toDto(MisuraPrevenzioneEventoRischioStakeholder entity, @Context CycleAvoidingMappingContext context);

    List<MisuraPrevenzioneEventoRischioStakeholderDTO> stakeholderToDto(List<MisuraPrevenzioneEventoRischioStakeholder> entities, @Context CycleAvoidingMappingContext context);
}


