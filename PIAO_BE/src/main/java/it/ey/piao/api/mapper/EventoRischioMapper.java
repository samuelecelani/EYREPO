package it.ey.piao.api.mapper;

import it.ey.dto.EventoRischioDTO;
import it.ey.dto.FattoreDTO;
import it.ey.entity.EventoRischio;
import it.ey.entity.Fattore;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    //*****+++Chiedere a Gianni
    uses = {CommonMapper.class, LivelloRischioMapper.class, AttivitaSensibileMapper.class, IMisuraPrevenzioneEventoRischioMapper.class}
)
public interface EventoRischioMapper {

    EventoRischioDTO toDto(EventoRischio entity,@Context CycleAvoidingMappingContext context);

    EventoRischio toEntity(EventoRischioDTO entity,@Context CycleAvoidingMappingContext context);

    List<EventoRischioDTO> toDtoList(List<EventoRischio> entities,@Context CycleAvoidingMappingContext context);

    List<EventoRischio> toEntityList(List<EventoRischioDTO> entities,@Context CycleAvoidingMappingContext context);

    // Mapping per Fattore (MongoDB)
    FattoreDTO fattoreToDto(Fattore entity,@Context CycleAvoidingMappingContext context);

    Fattore fattoreToEntity(FattoreDTO dto,@Context CycleAvoidingMappingContext context);

}
