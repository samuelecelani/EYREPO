package it.ey.piao.api.mapper;

import it.ey.dto.*;
import it.ey.entity.*;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;


/**
 * MapStruct mapper per Sezione23 e tutte le sue relazioni annidate.
 * Include: Obiettivi Prevenzione (con Misure e Indicatori), Obblighi Legge, Attività Sensibili
 * Usa CycleAvoidingMappingContext per prevenire cicli infiniti.
 * Usa SOLO CommonMapper centralizzato per evitare duplicazione di logica.
 */
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class,IObiettivoPrevenzioneCorruzioneTrasparenzaMapper.class, AttivitaSensibileMapper.class, MisuraPrevenzioneMapper.class,
                ObiettivoPrevenzioneMapper.class, ObbligoLeggeMapper.class,DatiPubblicatiMapper.class
            }
)
public interface Sezione23Mapper {

    /**
     * Mappa Sezione23 entity → DTO (include tutte le relazioni annidate)
     */
    @Mapping(target = "obiettivoPrevenzione", source = "obiettivoPrevenzione")
    @Mapping(target = "misuraPrevenzione", source = "misuraPrevenzione")
    @Mapping(target = "obblighiLegge", source = "obblighiLegge")
    @Mapping(target = "obiettivoPrevenzioneCorruzioneTrasparenza", source = "obiettivoPrevenzioneCorruzioneTrasparenza")
    @Mapping(target = "attivitaSensibile", source = "attivitaSensibile")
    @Mapping(target = "statoSezione", ignore = true) // Gestito manualmente
    @Mapping(target = "ulterioriInfo", ignore = true) // MongoDB - gestito separatamente
    @Mapping(target = "eventoRischio", ignore = true) // Gestito tramite attivitaSensibile.eventoRischio
    Sezione23DTO toDto(Sezione23 entity, @Context CycleAvoidingMappingContext context);

    /**
     * Mappa Sezione23 DTO → entity
     */
    @Mapping(target = "piao.id", source = "idPiao")
    @Mapping(target = "idStato", ignore = true) // Gestito manualmente
    @Mapping(target = "attivitaSensibile", ignore = true)
    @Mapping(target = "obiettivoPrevenzioneCorruzioneTrasparenza", ignore = true)
    @Mapping(target = "eventoRischio", ignore = true)
    @Mapping(target = "obblighiLegge", source = "obblighiLegge")
    Sezione23 toEntity(Sezione23DTO dto, @Context CycleAvoidingMappingContext context);
}
