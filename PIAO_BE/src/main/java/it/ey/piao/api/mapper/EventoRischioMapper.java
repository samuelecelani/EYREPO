package it.ey.piao.api.mapper;

import it.ey.dto.EventoRischioDTO;
import it.ey.dto.FattoreDTO;
import it.ey.entity.EventoRischio;
import it.ey.entity.Fattore;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class, LivelloRischioMapper.class, AttivitaSensibileMapper.class, IMisuraPrevenzioneEventoRischioMapper.class}
)
public interface EventoRischioMapper {

    @Mapping(target= "idLivelloRischio", source = "livelloRischio.id")
    @Mapping(target = "idAttivitaSensibile", source = "attivitaSensibile.id")
    EventoRischioDTO toDto(EventoRischio entity,@Context CycleAvoidingMappingContext context);


    @Mapping(target = "attivitaSensibile.id", source = "idAttivitaSensibile")
    EventoRischio toEntity(EventoRischioDTO entity,@Context CycleAvoidingMappingContext context);

    List<EventoRischioDTO> toDtoList(List<EventoRischio> entities,@Context CycleAvoidingMappingContext context);

    List<EventoRischio> toEntityList(List<EventoRischioDTO> entities,@Context CycleAvoidingMappingContext context);

    // Mapping per Fattore (MongoDB)
    FattoreDTO fattoreToDto(Fattore entity,@Context CycleAvoidingMappingContext context);

    Fattore fattoreToEntity(FattoreDTO dto,@Context CycleAvoidingMappingContext context);

}
