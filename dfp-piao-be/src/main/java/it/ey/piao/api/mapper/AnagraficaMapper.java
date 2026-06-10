package it.ey.piao.api.mapper;

import it.ey.dto.AnagraficaDTO;
import it.ey.entity.Anagrafica;
import it.ey.entity.Piao;
import it.ey.piao.api.mapper.util.CycleAvoidingMappingContext;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {CommonMapper.class}
)
public interface AnagraficaMapper {

    @Mapping(target = "idPiao", source = "idPiao.id")
    AnagraficaDTO toDto(Anagrafica entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "idPiao.id", source = "idPiao")
    Anagrafica toEntity(AnagraficaDTO dto, @Context CycleAvoidingMappingContext context);

    List<AnagraficaDTO> toDtoList(List<Anagrafica> entityList, @Context CycleAvoidingMappingContext context);

}
