package it.ey.piao.api.mapper;

import it.ey.dto.AnagraficaDTO;
import it.ey.entity.Anagrafica;
import it.ey.entity.Sezione1;
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

    @Mapping(target = "idSezione1", source = "idSezione1.id")
    AnagraficaDTO toDto(Anagrafica entity, @Context CycleAvoidingMappingContext context);

    @Mapping(target = "idSezione1.id", source = "idSezione1")
    Anagrafica toEntity(AnagraficaDTO dto, @Context CycleAvoidingMappingContext context);

    List<AnagraficaDTO> toDtoList(List<Anagrafica> entityList, @Context CycleAvoidingMappingContext context);

}

