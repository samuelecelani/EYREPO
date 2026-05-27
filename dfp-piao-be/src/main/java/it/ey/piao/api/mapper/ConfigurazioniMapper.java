package it.ey.piao.api.mapper;

import it.ey.dto.ConfigurazioniDTO;
import it.ey.entity.Configurazioni;
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
    uses = {CommonMapper.class}
)
public interface ConfigurazioniMapper {

    ConfigurazioniDTO toDto(Configurazioni entity, @Context CycleAvoidingMappingContext context);

    Configurazioni toEntity(ConfigurazioniDTO entity, @Context CycleAvoidingMappingContext context);

    List<ConfigurazioniDTO> toDtoList(List<Configurazioni> entities, @Context CycleAvoidingMappingContext context);

    List<Configurazioni> toEntityList(List<ConfigurazioniDTO> entities,@Context CycleAvoidingMappingContext context);

}
