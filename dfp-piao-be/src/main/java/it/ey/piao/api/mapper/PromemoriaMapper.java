package it.ey.piao.api.mapper;

import it.ey.dto.PromemoriaDTO;
import it.ey.entity.Promemoria;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface PromemoriaMapper
{
    PromemoriaDTO toDto(Promemoria entity, @Context CycleAvoidingMappingContext context);
    Promemoria toEntity(PromemoriaDTO dto, @Context CycleAvoidingMappingContext context);
    List<PromemoriaDTO> toEntityList(List<Promemoria> entityList, @Context CycleAvoidingMappingContext context);
    List<Promemoria> toDtoList(List<PromemoriaDTO> entityList, @Context CycleAvoidingMappingContext context);
}
